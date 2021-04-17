package ocha.itolab.hidden.applet.numdim;

import java.awt.image.BufferedImage;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.nio.Buffer;
import java.util.*;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.gl2.GLUT;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.gl2.GLUgl2;

import ocha.itolab.hidden.core.data.*;;


public class DimensionDrawer implements GLEventListener {
	
	public static int PCP = 1, SP = 2;
	
	private GL gl;
	private GLUT glut;
	private GLU glu;
	private GL2 gl2;
	private GLUgl2 glu2;
	GLAutoDrawable glAD;

	private int dragMode;
	
	DimensionTransformer trans = null;
	IndividualSet ps;

	int imageSize[] = new int[2];
//	double pos[] = new double[3];
//	double aspect = 1.0, im = -1.0;
		
	boolean isMousePressed = false/*, isAnnotation = true, isLigandFlag = true*/;
	int xposId = 0, yposId = 1;
	int drawType = PCP;
	
	DrawerUtility du = null;
	GLCanvas glcanvas;	

	double minmaxdiff[];
	
	IntBuffer view = IntBuffer.allocate(4);
	DoubleBuffer model = DoubleBuffer.allocate(16);
	DoubleBuffer proj = DoubleBuffer.allocate(16);
	DoubleBuffer objPos = DoubleBuffer.allocate(3);

	DimensionSP dsp;
	

	/**
	 */
	public DimensionDrawer(int width, int height, GLCanvas glc){
		glcanvas = glc;
		imageSize[0] = width;
		imageSize[1] = height;
		du = new DrawerUtility(width, height);
		
		view = IntBuffer.allocate(4);
		model = DoubleBuffer.allocate(16);
		proj = DoubleBuffer.allocate(16);

		glcanvas.addGLEventListener(this);
	}
	
	public GLAutoDrawable getGLAutoDrawable(){
		return glAD;
	}
	
	

	public void setTransformer(DimensionTransformer view) {
		this.trans = view;
//		du.setTransformer(view);
//		trans.setDrawerUtility(du);
	}
	

	public void setWindowSize(int width, int height) {
		imageSize[0] = width;
		imageSize[1] = height;
		du.setWindowSize(width, height);
	}
 
	public void setMousePressSwitch(boolean isMousePressed) {
		this.isMousePressed = isMousePressed;
	}
		

	public void setPosId(int x, int y) {
		xposId = x;   yposId = y;
		dsp.setPosId(x, y);
	}
	

	public void setIndividualSet(IndividualSet p) {
		ps = p;
		
		if(dsp == null)
			dsp  = new DimensionSP(gl, gl2, glu, glu2, glut, glAD);
		dsp.setIndividualSet(ps);
	}
	
	public void setEdgeRatio(double r) {
		dsp.setEdgeRatio(r);
	}

	
	public void setDrawType(int type) {
		drawType = type;
	}
	
	
	
	public void init(GLAutoDrawable drawable) {
		gl = drawable.getGL();
		glut = new GLUT();
		glu = new GLU();
		gl2= drawable.getGL().getGL2();
		glu2 = new GLUgl2();
		this.glAD = drawable;
		
		gl.glEnable(GL.GL_RGBA);
		gl2.glEnable(GL2.GL_DEPTH);
		gl2.glEnable(GL2.GL_DOUBLE);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl2.glEnable(GL2.GL_NORMALIZE);
		gl.glEnable(GL.GL_CULL_FACE);
		gl2.glDisable(GL2.GL_LIGHTING);
		
		gl.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
		
		dsp  = new DimensionSP(gl, gl2, glu, glu2, glut, glAD);
	}
	
	
	public void reshape(GLAutoDrawable drawable,
			int x, int y,
			int width, int height) {
		
		imageSize[0] = width;
		imageSize[1] = height;
		float ratio = (float)height / (float)width;
		
		gl.glViewport(0, 0, width, height);
		
		gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glLoadIdentity();
		gl2.glFrustum(-1.0f, 1.0f, -ratio, ratio, 5.0f, 40.0f);
		//gl.glOrtho(-2.0, 2.0, -2.0, 2.0, 0.0, 100.0);
		
		gl2.glMatrixMode(GL2.GL_MODELVIEW);
		gl2.glLoadIdentity();
		gl2.glTranslatef(0.0f, 0.0f, -20.0f);
		
		gl.glGetIntegerv(GL.GL_VIEWPORT, view);
		gl2.glGetDoublev(GL2.GL_MODELVIEW_MATRIX, model);
		gl2.glGetDoublev(GL2.GL_PROJECTION_MATRIX, proj);
		
	}
	
	public void mousePressed(int x, int y, int dragMode,int pressx,int pressy) {
		this.dragMode = dragMode;
	}
	
	public void mouseReleased(int x,int y,int dragMode,int releasex,int releasey) {
		this.dragMode = dragMode;
	}
	
	
	public void display(GLAutoDrawable drawable) {
		
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		
		gl2.glLoadIdentity();
		glu.gluLookAt( 0.0, 0.0, 20.0,
		           0.0, 0.0, 0.0,
		           0.0, 1.0, 0.0 );

		double shiftX = trans.getViewShift(0);
		double shiftY = trans.getViewShift(1);
		double scaleX = trans.getViewScaleX() * (double)imageSize[0] / 300.0;
		double scaleY = trans.getViewScaleY() * (double)imageSize[1] / 300.0;

		gl2.glPushMatrix();
		
		gl.glGetIntegerv(GL.GL_VIEWPORT, view);
		gl2.glGetDoublev(GL2.GL_MODELVIEW_MATRIX, model);
		gl2.glGetDoublev(GL2.GL_PROJECTION_MATRIX, proj);
		
		gl2.glTranslated(shiftX, shiftY, 0.0);
		gl2.glScaled(scaleX, scaleY, 1.0);
		
		dsp.draw(view, model, proj);
		
		gl2.glPopMatrix();
		
	}
	
	
	
	public void drawStroke(int xStart, int xNow, int yStart, int yNow) {
		dsp.drawStroke(xStart, xNow, yStart, yNow);
	}

	
	public ArrayList specifyDimensions() {
		return dsp.specifyDimensions();
	}
	


	@Override
	public void dispose(GLAutoDrawable arg0) {
		// TODO Auto-generated method stub
		
	}


}