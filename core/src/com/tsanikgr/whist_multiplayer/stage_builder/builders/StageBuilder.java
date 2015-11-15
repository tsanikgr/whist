package com.tsanikgr.whist_multiplayer.stage_builder.builders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.tsanikgr.whist_multiplayer.IPlatformApplication;
import com.tsanikgr.whist_multiplayer.IResolution;
import com.tsanikgr.whist_multiplayer.assets.IAssets;
import com.tsanikgr.whist_multiplayer.assets.LocalizationService;
import com.tsanikgr.whist_multiplayer.IStageBuilderListener;
import com.tsanikgr.whist_multiplayer.myactors.MyGroup;
import com.tsanikgr.whist_multiplayer.stage_builder.models.BaseActorModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.ButtonModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.CardModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.CheckBoxModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.CustomWidgetModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.ExternalGroupModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.GroupModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.ImageModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.LabelModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.PlaceHolderModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.SelectBoxModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.SliderModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.TextAreaModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.TextButtonModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.TextFieldModel;
import com.tsanikgr.whist_multiplayer.stage_builder.xml.XmlModelBuilder;
import com.tsanikgr.whist_multiplayer.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class StageBuilder {
	private static final String LANDSCAPE_LAYOUT_FOLDER = "layout-land";
	private static final String PORTRAIT_LAYOUT_FOLDER = "layout-port";
	private static final String DEFAULT_LAYOUT_FOLDER = "layout";
	public static final String ROOT_NAME = "ScreenRoot";

	private final Map<Class<? extends BaseActorModel>, ActorBuilder> builders = new HashMap<>();
	private final IAssets assets;
	private final IPlatformApplication app;
	private final IResolution resolution;
	private final LocalizationService localizationService;
	private final ExecutorService groupBuildingPool;
	private IStageBuilderListener stageBuilderListener;

	private final ReentrantLock lock = new ReentrantLock();

	public StageBuilder(IAssets assets, IPlatformApplication app) {
		this.assets = assets;
		this.app = app;
		this.resolution = assets.getResolution();
		this.localizationService = assets.getLocalization();

		registerWidgetBuilders(assets);
		groupBuildingPool = Executors.newFixedThreadPool(1);
	}

	public void switchOrientation() {

	}

	/**
	 * There must be a widget builder for every type of widget model. Models represents widget data and builders use this data to create scene2d actors.
	 *
	 * @param assets assets interface.
	 */
	private void registerWidgetBuilders(IAssets assets) {
		builders.put(ImageModel.class, new ImageBuilder(this.assets, this.resolution, this.localizationService));
		builders.put(GroupModel.class, new GroupBuilder(builders, assets, this.resolution, this.localizationService));
		builders.put(ButtonModel.class, new ButtonBuilder(this.assets, this.resolution, this.localizationService));
		builders.put(TextButtonModel.class, new TextButtonBuilder(this.assets, this.resolution, this.localizationService));
		builders.put(LabelModel.class, new LabelBuilder(this.assets, this.resolution, this.localizationService));
		builders.put(SelectBoxModel.class, new SelectBoxBuilder(this.assets, this.resolution, this.localizationService));
		builders.put(CustomWidgetModel.class, new CustomWidgetBuilder(this.assets, this.resolution, this.localizationService));
		builders.put(ExternalGroupModel.class, new ExternalGroupModelBuilder(this.assets, this.resolution, this.localizationService, this));
		builders.put(SliderModel.class, new SliderBuilder( this.assets, this.resolution, this.localizationService));
		builders.put(TextFieldModel.class, new TextFieldBuilder(assets, resolution, localizationService));
		builders.put(TextAreaModel.class, new TextAreaBuilder(assets, resolution, localizationService));
		builders.put(CheckBoxModel.class, new CheckBoxBuilder(assets, resolution, localizationService));
		builders.put(PlaceHolderModel.class, new PlaceHolderBuilder(assets, resolution, localizationService));
		builders.put(CardModel.class, new CardBuilder(assets, resolution,localizationService));
	}

	public MyGroup buildGroup(String fileName, String groupName, MyGroup groupToFill) throws Exception {
		if (assets.isRenderThread()) {
			while (!lock.tryLock(0, TimeUnit.SECONDS)) {
				assets.getAssetManager().update();
				app.executePendingUiThreadRunnables();
			}
		} else lock.lock();

		try {
			XmlModelBuilder xmlModelBuilder = new XmlModelBuilder();
			List<BaseActorModel> modelList = xmlModelBuilder.buildModels(getLayoutFile(fileName), groupName);
			if (modelList.size() == 0) return null;

			GroupModel groupModel = (GroupModel) modelList.get(0);
			MyGroup group;
			if (groupToFill == null) group = new MyGroup();
			else group = groupToFill;
			GroupBuilder groupBuilder = (GroupBuilder) builders.get(GroupModel.class);
			groupBuilder.setBasicProperties(groupModel, group);
			updateGroupSizeAndPosition(group, groupModel);
			for (BaseActorModel model : groupModel.getChildren()) {
				ActorBuilder builder = builders.get(model.getClass());
				group.addActor(builder.build(model));
			}
			return group;
		} finally {
			lock.unlock();
		}
	}

	public void buildGroupAsync(final String fileName, final String groupName, final MyGroup groupToFill){
		groupBuildingPool.execute(new GroupBuildingTask(fileName, groupName, groupToFill));
	}

	public IStageBuilderListener getStageBuilderListener() {
		return stageBuilderListener;
	}

	public void setStageBuilderListener(IStageBuilderListener stageBuilderListener) {
		this.stageBuilderListener = stageBuilderListener;
	}

	private class GroupBuildingTask implements Runnable {
		private final String fileName;
		private final String groupName;
		private final MyGroup groupToFill;

		private GroupBuildingTask(String fileName, String groupName, MyGroup groupToFill) {
			this.fileName = fileName;
			this.groupName = groupName;
			this.groupToFill = groupToFill;
		}

		@Override
		public void run() {
			try {
				MyGroup group = buildGroup(fileName, groupName, groupToFill);
				fireOnGroupBuilded(fileName, group);
			} catch (Exception e) {
				fireOnGroupBuildFailed(fileName, e);
			}
		}
	}

	private void fireOnGroupBuildFailed(String fileName, Exception e) {
		if(stageBuilderListener != null){
			stageBuilderListener.onGroupBuildFailed(fileName, e);
		}
	}

	private void fireOnGroupBuilded(String fileName, MyGroup group) {
		if(stageBuilderListener != null){
			stageBuilderListener.onGroupBuilded(fileName, group);
		}
	}

	private void updateGroupSizeAndPosition(MyGroup group, GroupModel referenceModel) {
		float multiplier = resolution.getPositionMultiplier();
		group.setX(referenceModel.getX() * multiplier);
		group.setY(referenceModel.getY() * multiplier);
		group.setWidth(referenceModel.getWidth() * multiplier);
		group.setHeight(referenceModel.getHeight() * multiplier);
	}

	public Stage build(String fileName, float width, float height, boolean keepAspectRatio) {
		FileHandle xmlFileHandle = getLayoutFile(fileName);
		boolean emptyStage = xmlFileHandle == null;
		if (emptyStage){
			new Log(this).i().append("constructing empty stage, no xmlFile specified").print();
		}
		try {
			GroupModel groupModel = null;
			if (!emptyStage) {
				XmlModelBuilder xmlModelBuilder = new XmlModelBuilder();
				List<BaseActorModel> modelList = xmlModelBuilder.buildModels(xmlFileHandle);
				groupModel = (GroupModel) modelList.get(0);
			}
			Stage stage;
			if (keepAspectRatio) stage = new Stage(new ExtendViewport(width, height));
			else stage = new Stage(new StretchViewport(width, height));

			MyGroup rootGroup= new MyGroup();
			if (!emptyStage) addActorsToStage(rootGroup, groupModel.getChildren());
			rootGroup.setName(ROOT_NAME);
			rootGroup.setX(resolution.getGameAreaPosition().x);
			rootGroup.setY(resolution.getGameAreaPosition().y);
			stage.addActor(rootGroup);
			return stage;
		} catch (Exception e) {
			throw new RuntimeException("Failed to build stage.", e);
		}
	}

	private void addActorsToStage(MyGroup rootGroup, List<BaseActorModel> models) {
		for (BaseActorModel model : models) {
			try {
				ActorBuilder builder = builders.get(model.getClass());
				rootGroup.addActor(builder.build(model));
			} catch (Exception e) {
				throw new RuntimeException("Failed to build stage on actor: " + model.getName(), e);
			}
		}
	}

	private FileHandle getLayoutFile(String fileName) {
		FileHandle fileHandle;
		String path;
		boolean isLandscape = resolution.getScreenWidth() > resolution.getScreenHeight();
		if (isLandscape) {
			path = LANDSCAPE_LAYOUT_FOLDER + "/" + fileName;
			fileHandle = Gdx.files.internal(path);
			if (fileExists(fileHandle)) {
				return fileHandle;
			}
		} else {
			path = PORTRAIT_LAYOUT_FOLDER + "/" + fileName;
			fileHandle = Gdx.files.internal(path);
			if (fileExists(fileHandle)) {
				return fileHandle;
			}
		}
		path = DEFAULT_LAYOUT_FOLDER + "/" + fileName;
		fileHandle = Gdx.files.internal(path);
		if (fileExists(fileHandle)) return fileHandle;
		else return null;
	}

	/**
	 * File.exists is too slow on Android.
	 * @param file
	 * @return true if file exists.
	 */
	private boolean fileExists(FileHandle file) {
		boolean exists = false;
		try {
			file.read().close();
			exists = true;
		} catch (Exception e) {
			//ignore
//			new Log(this).w("ignore",e,"fileExists");
		}
		return exists;
	}

	public IAssets getAssets() {
		return assets;
	}

	public IResolution getResolution() {
		return resolution;
	}

	public LocalizationService getLocalizationService() {
		return localizationService;
	}

	private static InputAdapter disableMultitouchAdapter = null;
	public static synchronized void disableMultiTouch(Stage stage) {

		if (disableMultitouchAdapter != null) return;

		disableMultitouchAdapter = new InputAdapter() {
			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
				return pointer > 0 || super.touchDown(screenX, screenY, pointer, button);
			}

			@Override
			public boolean touchDragged(int screenX, int screenY, int pointer) {
				return pointer > 0 || super.touchDragged(screenX, screenY, pointer);
			}

			@Override
			public boolean touchUp(int screenX, int screenY, int pointer, int button) {
				return pointer > 0 || super.touchUp(screenX, screenY, pointer, button);
			}
		};

		InputMultiplexer multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(disableMultitouchAdapter);
		multiplexer.addProcessor(stage);
		Gdx.input.setInputProcessor(multiplexer);
	}

	public static synchronized void enableMultiTouch(){

		if (disableMultitouchAdapter == null) return;
		InputProcessor inputProcessor = Gdx.input.getInputProcessor();

		if (inputProcessor == disableMultitouchAdapter) {
			Gdx.input.setInputProcessor(null);
		} else if (inputProcessor instanceof InputMultiplexer) {
			removeProcessor((InputMultiplexer)inputProcessor,disableMultitouchAdapter);
		}

		disableMultitouchAdapter = null;
	}

	private static void removeProcessor(InputMultiplexer multiplexer, InputProcessor processor){
		multiplexer.removeProcessor(processor);
		Array<InputProcessor> ips = multiplexer.getProcessors();
		for (int i = 0 ; i < ips.size ; i++) {
			if (ips.get(i) instanceof InputMultiplexer) removeProcessor((InputMultiplexer)ips.get(i), processor);
		}
	}
}
