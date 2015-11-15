package com.tsanikgr.whist_multiplayer.myactors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;

public class Mask{

	private static FrameBuffer maskingFrameBuffer = null;
	private static FrameBuffer getMaskingBuffer() {
		if (maskingFrameBuffer == null) maskingFrameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),false);
		return maskingFrameBuffer;
	}

	public static void disposeMaskingFrameBuffer(){
		if (maskingFrameBuffer != null) {
			maskingFrameBuffer.dispose();
			shaderProgram.dispose();
		}
		maskingFrameBuffer = null;
		shaderProgram = null;
	}

	private boolean doMask = false;
	Actor mask = null;
	private boolean doScissors = true;
	private final TextureRegion textureRegion = new TextureRegion();
	private final Vector2 gameAreaPosition;
	private final Matrix4 transformationMatrixTemp = new Matrix4();

	public Mask(Actor mask, Vector2 gameAreaPosition) {
		this(mask, true, gameAreaPosition);
	}

	public Mask(Actor mask, boolean doScissors, Vector2 gameAreaPosition) {
		this.gameAreaPosition = gameAreaPosition;
		this.mask = mask;
		this.doScissors = doScissors;
		mask.setVisible(false);
		mask.setTouchable(Touchable.disabled);
	}

	public void enableMask() {
		doMask = true;
	}

	public void disableMask() {
		mask.setVisible(false);
		doMask = false;
	}

	public Mask setScissors(boolean doScissors) {
		this.doScissors = doScissors;
		return this;
	}

	public void startMasking(Batch batch){
		if (!doMask) return;

		batch.flush();
		getMaskingBuffer().begin();

		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
		batch.setShader(maskingShader());
	}

	public void stopMasking(Batch batch) {
		stopMasking(batch, null);
	}

	public void stopMasking(Batch batch, Vector2 stageCoords){
		if (!doMask) return;

		batch.setBlendFunction(GL20.GL_ZERO, GL20.GL_SRC_COLOR);

		mask.setVisible(true);
		mask.draw(batch, 1);
		mask.setVisible(false);
		textureRegion.setRegion(getMaskingBuffer().getColorBufferTexture());
		textureRegion.flip(false, true);

		batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
		getMaskingBuffer().end();

		if (doScissors) {
			Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
			int x = (int) Math.ceil(mask.getX() + gameAreaPosition.x);
			int y = (int) Math.ceil(mask.getY() + gameAreaPosition.y);

			Gdx.gl.glScissor(x, y, (int) (mask.getWidth() - 2), (int) (mask.getHeight() - 2));
		}

		transformationMatrixTemp.set(batch.getTransformMatrix());
		batch.getTransformMatrix().idt();
		if (stageCoords == null) batch.draw(textureRegion,-gameAreaPosition.x,-gameAreaPosition.y);
		else batch.draw(textureRegion,-gameAreaPosition.x-stageCoords.x,-gameAreaPosition.y-stageCoords.y);
		batch.getTransformMatrix().set(transformationMatrixTemp);

		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		batch.setShader(null);
		if (doScissors) Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
	}

	private static ShaderProgram shaderProgram = null;
	private static ShaderProgram maskingShader() {
		if (shaderProgram == null ) {
			String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
					+ "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
					+ "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
					+ "uniform mat4 u_projTrans;\n" //
					+ "varying vec4 v_color;\n" //
					+ "varying vec2 v_texCoords;\n" //
					+ "\n" //
					+ "void main()\n" //
					+ "{\n" //
					+ "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
					+ "   v_color.a = v_color.a * (255.0/254.0);\n" //
					+ "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
					+ "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
					+ "}\n";

			String fragmentShader = "#ifdef GL_ES\n" //
					+ "#define LOWP lowp\n" //
					+ "precision mediump float;\n" //
					+ "#else\n" //
					+ "#define LOWP \n" //
					+ "#endif\n" //
					+ "varying LOWP vec4 v_color;\n" //
					+ "varying vec2 v_texCoords;\n" //
					+ "uniform sampler2D u_texture;\n" //
					+ "void main()\n"//
					+ "{\n" //
					+ "  vec4 initialColor = v_color * texture2D(u_texture, v_texCoords);\n" //
					+ "  gl_FragColor = vec4(initialColor.rgb * initialColor.a, initialColor.a);\n" //
					+ "}";
			shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
			if (!shaderProgram.isCompiled())
				throw new IllegalArgumentException("Error compiling shader: " + shaderProgram.getLog());
		}
		return shaderProgram;
	}
	public Actor getMask() {
		return mask;
	}
}
