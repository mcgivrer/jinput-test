package fr.snapgames.gdj.prototypes.jinput;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JToggleButton;

import net.java.games.input.Component;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

/**
 *
 * Joystick Test with JInput
 *
 *
 * @author TheUzo007 http://theuzo007.wordpress.com
 *
 *         Created 22 Oct 2013
 *
 */
public class JoystickTest {

	public static void main(String args[]) {
		new JoystickTest();
	}

	final JFrameWindow window;
	private ArrayList<Controller> foundControllers;

	public JoystickTest() {
		window = new JFrameWindow();

		foundControllers = new ArrayList<>();
		searchForControllers();

		// If at least one controller was found we start showing controller data on
		// window.
		if (!foundControllers.isEmpty())
			startShowingControllerData();
		else
			window.addControllerName("No controller found!");
	}

	/**
	 * Search (and save) for controllers of type Controller.Type.STICK,
	 * Controller.Type.GAMEPAD, Controller.Type.WHEEL and
	 * Controller.Type.FINGERSTICK.
	 */
	private void searchForControllers() {
		Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();

		for (int i = 0; i < controllers.length; i++) {
			Controller controller = controllers[i];

			if (controller.getType() == Controller.Type.STICK || controller.getType() == Controller.Type.GAMEPAD
					|| controller.getType() == Controller.Type.WHEEL
					|| controller.getType() == Controller.Type.FINGERSTICK) {
				// Add new controller to the list of all controllers.
				foundControllers.add(controller);

				// Add new controller to the list on the window.
				window.addControllerName(controller.getName() + " - " + controller.getType().toString() + " type");
			}
		}
	}

	/**
	 * Starts showing controller data on the window.
	 */
	private void startShowingControllerData() {
		while (true) {
			// Currently selected controller.
			int selectedControllerIndex = window.getSelectedControllerName();
			Controller controller = foundControllers.get(selectedControllerIndex);

			// Pull controller for current data, and break while loop if controller is
			// disconnected.
			if (!controller.poll()) {
				window.showControllerDisconnected();
				break;
			}

			// X axis and Y axis
			int xAxisPercentage = 0;
			int yAxisPercentage = 0;
			// RX axis and RY axis
			int rxAxisPercentage = 0;
			int ryAxisPercentage = 0;
			// JPanel for other axes.
			JPanel axesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 2));
			axesPanel.setBounds(0, 0, 200, 190);

			// JPanel for controller buttons
			JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
			buttonsPanel.setBounds(6, 19, 246, 110);

			// Go trough all components of the controller.
			Component[] components = controller.getComponents();
			for (int i = 0; i < components.length; i++) {
				Component component = components[i];
				Identifier componentIdentifier = component.getIdentifier();
				// Hat switch
				if (componentIdentifier == Component.Identifier.Axis.POV) {
					float hatSwitchPosition = component.getPollData();
					window.setHatSwitch(hatSwitchPosition);

					// We know that this component was hat switch so we can skip to next component.
					continue;
				}
				// Buttons
				if (!component.isAnalog()) {

					if (
							//for Linux compatibility
							componentIdentifier.equals(Component.Identifier.Button.A)
							|| componentIdentifier.equals(Component.Identifier.Button.B)
							|| componentIdentifier.equals(Component.Identifier.Button.X)
							|| componentIdentifier.equals(Component.Identifier.Button.Y)
							|| componentIdentifier.equals(Component.Identifier.Button.SELECT)
							|| componentIdentifier.equals(Component.Identifier.Button.MODE)
							|| componentIdentifier.equals(Component.Identifier.Button.START)
							|| componentIdentifier.equals(Component.Identifier.Button.LEFT_THUMB)
							|| componentIdentifier.equals(Component.Identifier.Button.RIGHT_THUMB)
							|| componentIdentifier.equals(Component.Identifier.Button.LEFT_THUMB3)
							|| componentIdentifier.equals(Component.Identifier.Button.RIGHT_THUMB3)
							// for windows compatibility !
							|| componentIdentifier.getName().matches("[0-9]+")) {

						// Is button pressed?
						boolean isItPressed = true;
						if (component.getPollData() == 0.0f)
							isItPressed = false;

						// Button index
						String buttonIndex;
						buttonIndex = component.getIdentifier().toString();
						if(isItPressed) {
							System.out.println("Digital button Identifier:"+buttonIndex);
						}

						// Create and add new button to panel.
						JToggleButton aToggleButton = new JToggleButton(buttonIndex, isItPressed);
						aToggleButton.setPreferredSize(new Dimension(48, 25));
						aToggleButton.setEnabled(true);
						buttonsPanel.add(aToggleButton);

						// We know that this component was button so we can skip to next component.
						continue;
					}
				}
				// Axes
				if (component.isAnalog()) {
					float axisValue = component.getPollData();
					int axisValueInPercentage = getAxisValueInPercentage(axisValue);

					// X axis
					if (componentIdentifier == Component.Identifier.Axis.X) {
						xAxisPercentage = axisValueInPercentage;
						continue; // Go to next component.
					}
					// Y axis
					if (componentIdentifier == Component.Identifier.Axis.Y) {
						yAxisPercentage = axisValueInPercentage;
						continue; // Go to next component.
					}

					// X axis
					if (componentIdentifier == Component.Identifier.Axis.RX) {
						rxAxisPercentage = axisValueInPercentage;
						continue; // Go to next component.
					}
					// Y axis
					if (componentIdentifier == Component.Identifier.Axis.RY) {
						ryAxisPercentage = axisValueInPercentage;
						continue; // Go to next component.
					}

					// Other axis
					JLabel progressBarLabel = new JLabel(component.getName());
					JProgressBar progressBar = new JProgressBar(0, 100);
					progressBar.setValue(axisValueInPercentage);
					axesPanel.add(progressBarLabel);
					axesPanel.add(progressBar);
				}
			}

			// Now that we go trough all controller components,
			// we add butons panel to window,
			window.setControllerButtons(buttonsPanel);
			// set x and y axes,
			window.setXYAxis(xAxisPercentage, yAxisPercentage);
			// set x and y axes,
			window.setRXRYAxis(rxAxisPercentage, ryAxisPercentage);
			// add other axes panel to window.
			window.addAxisPanel(axesPanel);

			// We have to give processor some rest.
			try {
				Thread.sleep(25);
			} catch (InterruptedException ex) {
				Logger.getLogger(JoystickTest.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	/**
	 * Given value of axis in percentage. Percentages increases from left/top to
	 * right/bottom. If idle (in center) returns 50, if joystick axis is pushed to
	 * the left/top edge returns 0 and if it's pushed to the right/bottom returns
	 * 100.
	 * 
	 * @return value of axis in percentage.
	 */
	public int getAxisValueInPercentage(float axisValue) {
		return (int) (((2 - (1 - axisValue)) * 100) / 2);
	}
}
