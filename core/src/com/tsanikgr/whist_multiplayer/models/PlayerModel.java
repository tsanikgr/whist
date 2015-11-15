package com.tsanikgr.whist_multiplayer.models;

import com.badlogic.gdx.utils.Array;

public class PlayerModel {

	private int id;
	private String name;
	private Array<Integer> cards;
	private final int[] score;
	private final int[] declaredNo;
	private final int[] achievedNo;

	// no-arg constructor needed for deserialization
	private PlayerModel(){
		this(-1);
	}

	public PlayerModel(int id) {
		this.id = id;
		name = "Robot " + (id+1);
		cards = new Array<>();
		score = new int[14];
		declaredNo = new int[14];
		achievedNo = new int[14];
	}

	public void reset() {
		for (int i = 0 ; i < 14; i++) {
			score[i] = -1;
			declaredNo[i] = -1;
			achievedNo[i] = -1;
		}
		cards.clear();
	}

	public void receiveCards(Array<Integer> newCards){
		cards.addAll(newCards);
	}
	public Array<Integer> getCards(){
		return cards;
	}
	public boolean isUser() {
		return id == 0;
	}
	public int getDeclaredNo(int round) {
		return declaredNo[round-1];
	}
	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public int getScore(int round) {
		return score[round-1];
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setCards(Array<Integer> cards) {
		this.cards = cards;
	}
	public void setScore(int score, int round) {
		this.score[round-1] = score;
	}
	public void setDeclaredNo(int declaredNo, int round) {
		this.declaredNo[round-1] = declaredNo;
	}
	public void setAchievedNo(int achievedNo, int round) {
		this.achievedNo[round-1] = achievedNo;
	}
	public int getAchievedNo(int round) {
		return achievedNo[round-1];
	}
	public void incrementAchievedNo(int round) {
		achievedNo[round-1]++;
	}
	public boolean hasCard(int card) {
		return cards.contains(card,true);
	}
	public void throwCard(int card) {
		cards.removeValue(card,true);
	}
	public boolean isWorseScore(int round) {
		if (round == 1) return score[0] < 0;
		return score[round - 1] < score[round - 2];
	}

	public void copy(PlayerModel player) {
		this.id = player.id;
		this.name = player.name;
		this.cards.clear();
		this.cards.addAll(player.cards);
		for (int i = 0; i < 14; i++) {
			this.score[i] = player.score[i];
			this.declaredNo[i] = player.declaredNo[i];
			this.achievedNo[i] = player.achievedNo[i];
		}
	}
}
