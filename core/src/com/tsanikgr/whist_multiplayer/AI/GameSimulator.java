package com.tsanikgr.whist_multiplayer.AI;

import com.badlogic.gdx.utils.Array;
import com.tsanikgr.whist_multiplayer.models.BoardModel;
import com.tsanikgr.whist_multiplayer.models.GameModel;
import com.tsanikgr.whist_multiplayer.models.GameState;
import com.tsanikgr.whist_multiplayer.models.PlayerModel;
import com.tsanikgr.whist_multiplayer.myactors.Card;
import com.tsanikgr.whist_multiplayer.util.Log;

public class GameSimulator extends AbstractWhistAI implements ValidateAiCallback {

	private static final int GAME_POOL_INIT_CAPACITY = 21;
	private static final int ARRAY_INT_POOL_INIT_CAPACITY = 100;
	private static final int ARRAY_INT_POOL_MAX_CAPACITY = 100;
	private static final int GAME_MODEL_POOL_MAX_CAPACITY = 100;
	private static final int LOG_EVERY_SIMULATIONS = 50000;
	private static final int EXECUTOR_POLLING_MILLIS = 200;
	private static final int SKIP_DECLARATION = -10;

	private final AiConfig aiConfig;
	private final WhistExecutorService executorService;
	private final ThreadedGameModelPool gameModelPool;
	private final ThreadedArrayIntegerPool integerArrayPool;

	private final Array<Integer> totalSimsPerStartingCard;
	private final Array<Integer> startingCards;

	private boolean cancelNow;
	private int startingRound;
	private int startingCardsN;
	private int whichPlayer;
	private double logCounter = LOG_EVERY_SIMULATIONS;

	// need synchronization
	private double totalSimulationsPerformed;
	private long[][] bausenWon;
	private long[][][] bausenWonPerCard;

	public GameSimulator(AiConfig config, WhistAICallback callback, float minimumDelay) {
		super(callback, minimumDelay);
		this.aiConfig = config;
		this.startingCards = new Array<>();
		this.totalSimsPerStartingCard = new Array<>();
		this.startingRound = -1;

		gameModelPool = new ThreadedGameModelPool(aiConfig.getMaxThreads(), GAME_POOL_INIT_CAPACITY, GAME_MODEL_POOL_MAX_CAPACITY);
		integerArrayPool = new ThreadedArrayIntegerPool(aiConfig.getMaxThreads(), ARRAY_INT_POOL_INIT_CAPACITY, ARRAY_INT_POOL_MAX_CAPACITY);
		executorService = new WhistExecutorService(aiConfig.getMaxThreads(), aiConfig.getSpawnThreadIfCardsLeftMoreThan(), gameModelPool, integerArrayPool, new ValidateAiCallback(){
			@Override
			public boolean validateGameAction(ThreadedGameModelPool.SimGameModel model, int player, boolean isDeclareResult, int result) {
				return GameSimulator.this.validateGameAction(model, player, isDeclareResult, result);
			}
		});
	}

	private ThreadedGameModelPool.SimGameModel init(GameModel model){
		whichPlayer = model.getState().getCurrentPlayer();
		startingCardsN = model.getPlayer(whichPlayer).getCards().size;
		startingRound = model.getState().getRound();
		totalSimulationsPerformed = 0;
		bausenWon = new long[4][model.getState().getRound() + 1];
		for (int p = 0; p < 4; p++)
			for (int b = 0; b < model.getState().getRound() + 1; b++) bausenWon[p][b] = 0;
		bausenWonPerCard = new long[4][startingRound][startingRound + 1];
		startingCards.clear();
		totalSimsPerStartingCard.clear();

		ThreadedGameModelPool.SimGameModel newModel = gameModelPool.obtain().copy(model, true);
		startingCards.addAll(newModel.getPlayer(model.getState().getCurrentPlayer()).getCards());
		for (int i = 0; i < startingCards.size; i++) totalSimsPerStartingCard.add(0);
		for (int p = 0; p < 4; p++) {
			if (newModel.getPlayer(p).getAchievedNo(startingRound) == -1)
				newModel.getPlayer(p).setAchievedNo(0, startingRound);
		}
		return newModel;
	}

	public void stop() {
		if (executorService != null) executorService.timeout(0,10000);
		cancelNow = true;
	}

	/** ***********************************************************************************/

	@Override
	public int requestDeclaration(GameModel model) {
		ThreadedGameModelPool.SimGameModel newModel = init(model);
//		if (newModel == null) return getBestDeclaration(model);

		cancelNow = false;
		executorService.start();
		requestAction(newModel);
		executorService.waitForThreads(EXECUTOR_POLLING_MILLIS, aiConfig.getExecutorTimeoutSeconds());

		logResultsDeclaration(model.getState().getRound());
		return getBestDeclaration(model);   //model, NOT newModel!
	}

	private int getBestDeclaration(GameModel model) {
		int restriction = getRestriction(model);
		int cp = model.getState().getCurrentPlayer();
		int best = 0;
		for (int b = 1; b < model.getState().getRound() + 1; b++)
			if (bausenWon[cp][b] > bausenWon[cp][best]) best = b;
		if (restriction == best) {
			if (best - 1 < 0) best++;
			else if (best + 1 > model.getState().getRound()) best--;
			else best = (bausenWon[cp][best - 1] > bausenWon[cp][best + 1] ? best - 1 : best + 1);
		}
		return best;
	}

	/** ***********************************************************************************/

	@Override
	public int requestWhichCardToPlay(GameModel model){
		log.i().append(">>>>>>>>> Player [").append(model.getState().getCurrentPlayer()).append("]").print();
		log.i().append("________________________________________").print();
		int best;

		Array<Integer> validCards = integerArrayPool.obtainSync(aiConfig.getMaxThreads());
		getValidCards(model, validCards);
		best = validCards.get(0);     //in one of the following step fails
		try {
			if (validCards.size == 1) {
				log.i().append("Only one possible card.").print();
				return best;
			}
			if (validCards.size == 2 && Math.abs(compareForWistWinner(validCards.get(0), validCards.get(1), Card.getSuit(model.getState().getAttou()))) == 1) {
				log.i().append("No difference between possible cards.").print();
				return best;
			}
			ThreadedGameModelPool.SimGameModel newModel = init(model);
			cancelNow = false;
			executorService.start();
			requestAction(newModel);
			executorService.waitForThreads(EXECUTOR_POLLING_MILLIS, aiConfig.getExecutorTimeoutSeconds());
			best = getBestCard(model, validCards);
			log.i().append("Freeing gameModel of primary worker thread.").print();
		} finally {
			integerArrayPool.free(validCards, aiConfig.getMaxThreads());
		}
		return best;
	}

	private int getBestCard(GameModel model, Array<Integer> validCards) {
		long sum = 0;
		int cp = model.getState().getCurrentPlayer();
		int best = 0;
		for (int c = 0; c < validCards.size; c++) {
			for (int b = 0; b < startingRound + 1; b++)
				sum += bausenWonPerCard[cp][startingCards.indexOf(validCards.get(c), true)][b];
			if (bausenWonPerCard[cp]
					[startingCards.indexOf(validCards.get(c), true)]
					[model.getPlayer(cp).getDeclaredNo(model.getState().getRound())] > bausenWonPerCard[cp]
					[startingCards.indexOf(validCards.get(best), true)]
					[model.getPlayer(cp).getDeclaredNo(model.getState().getRound())]) best = c;
		}

		logResultsGetBestCard(model, validCards, cp, best, sum);
		return validCards.get(best);
	}

	/** ***********************************************************************************/

	private void requestAction(ThreadedGameModelPool.SimGameModel model){
		if (model.getState().isDeclaringBausen())
			validateGameAction(model, model.getState().getCurrentPlayer(), true, SKIP_DECLARATION);
		else simulateGameModel(model);
	}

	/** ***********************************************************************************/
	private void simulateGameModel(ThreadedGameModelPool.SimGameModel model){
		int threadId = model.threadId;
		Array<Integer> validCards = integerArrayPool.obtainSync(threadId);
		try {
			getValidCards(model, validCards);
			filterValidCards(model, validCards);

			ThreadedGameModelPool.SimGameModel[] simGameModelCache = new ThreadedGameModelPool.SimGameModel[validCards.size];
			for (int i = 0; i < validCards.size; i++) simGameModelCache[i] = gameModelPool.fromPrototype(model);
			gameModelPool.free(model);
			for (int i = 0; i < validCards.size; i++) simulateModelInNewThreadIfRequired(simGameModelCache[i], validCards.get(i), validCards.size);
		} finally {
			integerArrayPool.free(validCards, threadId);
		}
	}

	private void simulateModelInNewThreadIfRequired(ThreadedGameModelPool.SimGameModel model, int card, int nValidCardSiblings) {
		if ((model.getState().getCardsLeft() == startingCardsN) && (model.getState().getCurrentPlayer() == whichPlayer)) {
			model.startingCard = model.getPlayer(whichPlayer).getCards().indexOf(card, true);
			if (model.startingCard != -1) totalSimsPerStartingCard.set(model.startingCard, 0);
		}

		/** check for early termination */
		if (model.startingCard != -1 && totalSimsPerStartingCard.get(model.startingCard) == -1 || Thread.currentThread().isInterrupted() || cancelNow) {
			gameModelPool.free(model);
			return;
		}

		if (nValidCardSiblings > 1 && executorService.execute(model, card)) return;   /** see if we can do it in another thread */
		validateGameAction(model, model.getState().getCurrentPlayer(), false, card);      /** If no more threads are allowed */
	}

	/** ***********************************************************************************/

	private void getValidCards(GameModel model, Array<Integer> validCards){
		PlayerModel player = model.getPlayer(model.getState().getCurrentPlayer());
		Array<Integer> cards = player.getCards();

		for (int i = 0; i < cards.size; i++)
			if (isValidThrow(player, model.getState(), cards.get(i)))
				validCards.add(cards.get(i));
	}

	private void filterValidCards(GameModel model, Array<Integer> validCards){
		if (validCards.size == 1) return;
		if (validCards.size > aiConfig.getMaxFilteredCards() && model.getState().getCardsLeft() >= aiConfig.getFilterAboveRound())
			validCards.removeRange(aiConfig.getMaxFilteredCards(), validCards.size - 1);
	}

	/** ***********************************************************************************/

	private void newTrick(GameModel model, int firstPlayer) {
		model.getState().setTrickFirstPlayer(firstPlayer);
		model.getState().setFollowSuit(Card.Suit.NONE);
		model.getState().setCurrentPlayer(model.getState().getTrickFirstPlayer());
		model.getBoard().reset();
		model.getBoard().setFirstPlayerOffset(model.getState().getCurrentPlayer());
	}

	@Override
	public boolean validateGameAction(ThreadedGameModelPool.SimGameModel model, int player, boolean isDeclareResult, int result) {

		if ((player != model.getState().getCurrentPlayer()) ||
		 ((isDeclareResult && !validateDeclaration(model, player, result)) || (!isDeclareResult && !validatePlay(model, player, result)))){
			gameModelPool.free(model);
			return false;
		}

		if (model.getState().getCardsLeft() == 0) endOfRound(model);
		else requestAction(model);

		return true;
	}

	private int getRestriction(GameModel model){
		int restriction = isTrickLastPlayer(model.getState()) ? model.getState().getRound() - model.getState().getTotalBausen() : -1;
		if (model.getState().getRound() == 14) restriction--;

		return Math.max(restriction,-1);
	}

	private boolean validateDeclaration(GameModel model,  int player, int declaration) {

		if (!model.getState().isDeclaringBausen()) return false;
		if (!(declaration >= 0 && declaration <= Math.min(model.getState().getRound(),13) && getRestriction(model) != declaration) && declaration != SKIP_DECLARATION) return false;

		model.getPlayer(player).setDeclaredNo(declaration, model.getState().getRound());
		model.getState().addToTotalBausen(declaration);
		if (isTrickLastPlayer(model.getState())) onLastPlayerDeclared(model);
		else model.getState().setCurrentPlayer(nextPlayer(player));
		return true;
	}

	private boolean validatePlay(GameModel model, int player, int card) {

		if (model.getState().isDeclaringBausen()) return false;
		if (!isValidThrow(model.getPlayer(player), model.getState(), card)) return false;

		model.getPlayer(player).throwCard(card);
		model.getBoard().setCard(player, card);
		if (getMiddleCardsNo(model.getBoard()) == 1) model.getState().setFollowSuit(getFirstSuit(model.getBoard()));
		if (isTrickLastPlayer(model.getState())) onLastPlayerPlayed(model);
		else model.getState().setCurrentPlayer(nextPlayer(model.getState().getCurrentPlayer()));
		return true;
	}

	private void onLastPlayerDeclared(GameModel model) {
		model.getState().setDeclaringBausen(false);
		newTrick(model, model.getState().getTrickFirstPlayer());
	}

	private void onLastPlayerPlayed(GameModel model){
		newTrick(model, findWinner(model));
		model.getState().decrementCardsLeft();
	}

	private int findWinner(GameModel model){
		int winner = getWinner(model.getBoard(), Card.getSuit(model.getState().getAttou()));
		model.getPlayer(winner).incrementAchievedNo(model.getState().getRound());
		return winner;
	}

	private int getWinner(BoardModel board, int attouSuit) throws RuntimeException {
		if (getMiddleCardsNo(board) != 4) throw new RuntimeException("Can't get winner without 4 cards on the board");
		int winner = board.getFirstPlayerOffset();
		for (int c =  0 ; c < 4 ; c++)
			if ((c != winner) && (compareForWistWinner(board.getCard(winner), board.getCard(c),	attouSuit) < 0)) winner = c;
		return winner;
	}


	private synchronized void endOfRound(ThreadedGameModelPool.SimGameModel model) {
		for (int p = 0; p < 4; p++) {
			bausenWon[p][model.getPlayer(p).getAchievedNo(startingRound)]++;
			if (p == whichPlayer && model.startingCard != -1)
				bausenWonPerCard[p][model.startingCard][model.getPlayer(p).getAchievedNo(startingRound)]++;
		}

		totalSimulationsPerformed++;
		if (model.startingCard != -1) {
			totalSimsPerStartingCard.set(model.startingCard, totalSimsPerStartingCard.get(model.startingCard)+1);
			if (totalSimsPerStartingCard.get(model.startingCard) > aiConfig.getMaxSimsPerCard()) totalSimsPerStartingCard.set(model.startingCard, -1);
		}

		if (totalSimulationsPerformed > logCounter) logEndOfRoundResults(model);
		else if (logCounter - totalSimulationsPerformed > LOG_EVERY_SIMULATIONS) logCounter = totalSimulationsPerformed+LOG_EVERY_SIMULATIONS;

		gameModelPool.free(model);
	}

	private int getFirstSuit(BoardModel board){
		if (board.getCard(board.getFirstPlayerOffset()) != -1)
			return Card.getSuit(board.getCard(board.getFirstPlayerOffset()));
		else
			return Card.Suit.NONE;
	}

	private int getMiddleCardsNo(BoardModel board) {
		int count = 0;
		for (int i = 0 ; i < 4 ; i++) if (board.getCard(i) != -1) count++;
		return count;
	}

	private int nextPlayer(int currentPlayer) {
		return (currentPlayer+1)%4;
	}

	private boolean isTrickLastPlayer(GameState state){
		return state.getCurrentPlayer() == (state.getTrickFirstPlayer()+3)%4;
	}

	@SuppressWarnings("SimplifiableIfStatement")
	private boolean isValidThrow(PlayerModel playerModel, GameState state, int card) {
		if (playerModel.getCards().size != state.getCardsLeft()) return false;
		if (!playerModel.hasCard(card)) return false;
		if (state.getFollowSuit() == Card.Suit.NONE || Card.getSuit(card) == state.getFollowSuit()) return true;
		return !hasSuit(playerModel, state.getFollowSuit());
	}

	private boolean hasSuit(PlayerModel player, int suit) {
		for (int i = 0 ; i < player.getCards().size ; i++) if (Card.getSuit(player.getCards().get(i)) == suit) return true;
		return false;
	}

	private void logResultsDeclaration(int round) {
		long sum;

		sum = 0;
		String sims;
		if (totalSimulationsPerformed > 1000000) sims = String.format("%.1fM", totalSimulationsPerformed/1000000f);
		else if (totalSimulationsPerformed > 1000) sims = String.format("%.1fK", totalSimulationsPerformed/1000f);
		else sims = String.format("%.0f",totalSimulationsPerformed);

		for (int b = 0; b < round + 1; b++) sum += bausenWon[whichPlayer][b];
		log.i().print();
		log.i().append("________________________________________").print();
		log.i().append("+++++++++ Player [").append(whichPlayer).append("], ").append(sims).append(" simulations").print();
		Log.LogEntry splits = log.i();
		for (int b = 0; b < round + 1; b++) {
			if ((float)bausenWon[whichPlayer][b]/(float) sum < 0.1) continue;
			splits.append("|  ").append(b).append(b>9 ? "  " : "   ");
		}
		splits.print();
		Log.LogEntry message = log.i();
		for (int b = 0; b < round + 1; b++) {
			if ((float)bausenWon[whichPlayer][b]/(float)sum < 0.1) continue;
			message.append("| ").append((float)bausenWon[whichPlayer][b]/(float)sum,2).append(" ");
		}
		message.print();
	}

	private void logResultsGetBestCard(GameModel model, Array<Integer> validCards, int currentPlayer, int best, long sum) {
		String sims;
		if (totalSimulationsPerformed > 1000000) sims = String.format("%.1fM", totalSimulationsPerformed/1000000f);
		else if (totalSimulationsPerformed > 1000) sims = String.format("%.1fK", totalSimulationsPerformed/1000f);
		else sims = String.format("%.0f",totalSimulationsPerformed);

		log.i().append("certainty: ").
				append((float) (bausenWonPerCard[currentPlayer][startingCards.indexOf(validCards.get(best), true)][model.getPlayer(currentPlayer).getDeclaredNo(model.getState().getRound())]) / (float) sum * 100f, 2).
				append("%").print();
		log.i().append("simulations: ").append(sims).print();
	}

	private void logEndOfRoundResults(ThreadedGameModelPool.SimGameModel model){
		logCounter+=LOG_EVERY_SIMULATIONS;
		String sims;
		if (totalSimulationsPerformed > 1000000) sims = String.format("%.1fM", totalSimulationsPerformed/1000000f);
		else if (totalSimulationsPerformed > 1000) sims = String.format("%.1fK", totalSimulationsPerformed/1000f);
		else sims = String.format("%.0f",totalSimulationsPerformed);
		Log.LogEntry logEntry = log.i().append("simulations from thread [").append(model.threadId).append("] (total/card): ").append(sims).append(" / ");
		if (model.startingCard != -1) {
			if (totalSimsPerStartingCard.get(model.startingCard) > 1000000)
				sims = String.format("%.1fM", totalSimsPerStartingCard.get(model.startingCard) / 1000000f);
			else if (totalSimsPerStartingCard.get(model.startingCard) > 1000)
				sims = String.format("%.1fK", totalSimsPerStartingCard.get(model.startingCard) / 1000f);
			else sims = Integer.toString(totalSimsPerStartingCard.get(model.startingCard));
		}
		logEntry.append(sims).print();
	}

	private int compareForWistWinner(int reference, int test, int attouSuit) {
		int mRank = Card.getRank(reference,true);
		int cRank = Card.getRank(test, true);
		if (attouSuit != Card.Suit.NONE) {
			if (Card.getSuit(reference) == attouSuit) mRank += 14;
			if (Card.getSuit(test) == attouSuit) cRank += 14;
			else if (Card.getSuit(reference) != Card.getSuit(test) && Card.getSuit(reference) != attouSuit) cRank -= 14;
		} else if (Card.getSuit(reference) != Card.getSuit(test)) cRank -= 14;
		return mRank - cRank;
	}
}
