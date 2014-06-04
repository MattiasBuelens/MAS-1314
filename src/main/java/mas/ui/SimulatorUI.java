package mas.ui;

import mas.Packet;
import mas.Truck;

import org.eclipse.swt.graphics.RGB;

import rinde.sim.core.Simulator;
import rinde.sim.scenario.ScenarioController.UICreator;
import rinde.sim.ui.View;
import rinde.sim.ui.renderers.GraphRoadModelRenderer;
import rinde.sim.ui.renderers.PDPModelRenderer;
import rinde.sim.ui.renderers.RoadUserRenderer;
import rinde.sim.ui.renderers.UiSchema;

public class SimulatorUI implements UICreator {

	@Override
	public void createUI(Simulator sim) {
		final UiSchema uis = new UiSchema();
		uis.add(Truck.class, "/graphics/perspective/empty-truck-32.png");
		uis.add(Packet.class, "/graphics/perspective/deliverypackage.png");
		uis.add(MessagingLayerRenderer.RADIUS_COLOR, new RGB(0, 255, 0));

		final View.Builder viewBuilder = View.create(sim).with(
				new GraphRoadModelRenderer(), new MessagingLayerRenderer(uis),
				new RoadUserRenderer(uis, false), new PDPModelRenderer(true));

		viewBuilder.show();
	}

}
