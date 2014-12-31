package app;

import com.jme3.bullet.BulletAppState;
import java.util.logging.Level;
import java.util.logging.Logger;

import math.MyRandom;
import model.Model;
import tools.LogUtil;
import view.View;
import view.mapDrawing.MapRenderer;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.system.AppSettings;
import controller.Controller;
import controller.battlefield.BattleFieldController;
import controller.editor.EditorController;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainRTS extends MySimpleApplication implements ActionListener{
        Model model;
	View view;
	MapRenderer tr;
	BattleFieldController fieldCtrl;
        EditorController editorCtrl;
        Controller actualCtrl;

	public static void main(String[] args) {
		AppSettings settings = new AppSettings(true);
		settings.setBitsPerPixel(32);
		settings.setWidth(1200);
		settings.setHeight(600);
		settings.setTitle("RTS");
		settings.setVSync(true);
		Logger.getLogger("").setLevel(Level.INFO);
		LogUtil.init();
		LogUtil.logger.info("seed : " + MyRandom.SEED);

		MainRTS app = new MainRTS();
		app.setShowSettings(false);
		app.setSettings(settings);
		app.start();
	}
        
        @Override
	public void simpleInitApp() {
                bulletAppState = new BulletAppState();
                stateManager.attach(bulletAppState);
                bulletAppState.getPhysicsSpace().setGravity(new Vector3f(0, 0, -1));

		flyCam.setUpVector(new Vector3f(0, 0, 1));
		flyCam.setEnabled(false);

                model = new Model();
                view = new View(rootNode, guiNode, bulletAppState.getPhysicsSpace(), assetManager, viewPort, model);
                model.addListener(view);
                
                NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort);

                fieldCtrl = new BattleFieldController(model, view, niftyDisplay.getNifty(), inputManager, cam);
                fieldCtrl.register(this);
                editorCtrl = new EditorController(model, view, niftyDisplay.getNifty(), inputManager, cam);
                editorCtrl.register(this);
                
                actualCtrl = editorCtrl;
                actualCtrl.activate();
		
                view.mapRend.renderTiles();
                
                guiViewPort.addProcessor(niftyDisplay);
                
	}
        
        @Override
        public void simpleUpdate(float tpf) {
            float maxedTPF = Math.min(tpf, 0.1f);
            view.actorManager.render();
            actualCtrl.update(maxedTPF);
            
            model.updateConfigs();
        }

	@Override
	public void destroy() {
	}

	@Override
	public void prePhysicsTick(PhysicsSpace arg0, float arg1) {
	}

    @Override
    public void actionPerformed(ActionEvent e) {
        LogUtil.logger.info("switching controller...");
        Controller desiredCtrl;
        switch(e.getActionCommand()){
            case "CTRL1" : desiredCtrl = fieldCtrl; break;
            case "CTRL2" : desiredCtrl = editorCtrl; break;
            case "CTRL3" : desiredCtrl = null; break;
                default:throw new IllegalAccessError();
        }
        
        if(desiredCtrl == null)
            return;
        
        actualCtrl.desactivate();
        actualCtrl = desiredCtrl;
        actualCtrl.activate();
    }
}
