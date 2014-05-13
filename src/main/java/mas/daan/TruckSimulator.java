

package mas.daan;

import javax.measure.Measure;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.eclipse.swt.graphics.RGB;

import rinde.sim.core.Simulator;
import rinde.sim.core.TickListener;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Graph;
import rinde.sim.core.graph.LengthData;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.communication.CommunicationModel;
import rinde.sim.core.model.road.GraphRoadModel;
import rinde.sim.core.model.road.MovingRoadUser;
import rinde.sim.core.model.road.PlaneRoadModel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.examples.core.comm.AgentCommunicationExample;
import rinde.sim.examples.core.comm.ExamplePackage;
import rinde.sim.examples.core.comm.MessagingLayerRenderer;
import rinde.sim.serializers.DotGraphSerializer;
import rinde.sim.serializers.SelfCycleFilter;
import rinde.sim.ui.View;
import rinde.sim.ui.renderers.GraphRoadModelRenderer;
import rinde.sim.ui.renderers.PlaneRoadModelRenderer;
import rinde.sim.ui.renderers.RoadUserRenderer;
import rinde.sim.ui.renderers.UiSchema;

/**
 * @author Daan Gheysens & Mattias Buelens
 */
public class TruckSimulator {

  // speed in km/h
  static final double VEHICLE_SPEED = 50d;
  
  private static final String MAP_DIR = "/data/maps/leuven-simple.dot";
  private static final int NUM_AGENTS = 50;

  // vehicle speed
  private static final double MIN_SPEED = 50d;
  private static final double MAX_SPEED = 100d;

  // communication range
  private static final int MIN_RADIUS = 3000;
  private static final int MAX_RADIUS = 12000;

  // communication reliability
  private static final double MIN_RELIABILITY = .01;
  private static final double MAX_RELIABILITY = .6;
  

  private TruckSimulator() {}

  /**
   * Starts the example.
   * @param args This is ignored.
   */
  public static void main(String[] args) {
    run(false);
  }

  public static void run(boolean testing) {
    // initialize a random generator which we use throughout this
    // 'experiment'
    final RandomGenerator rnd = new MersenneTwister(123);

    // initialize a new Simulator instance
    final Simulator sim = new Simulator(rnd, Measure.valueOf(1000L,
        SI.MILLI(SI.SECOND)));

    // register a PlaneRoadModel, a model which facilitates the moving of
    // RoadUsers on a plane. 
    
    //TODO TRY/CATCH of Exception onderzoeken
    try {
    final Graph<LengthData> graph = DotGraphSerializer
            .getLengthGraphSerializer(new SelfCycleFilter()).read(
                TruckSimulator.class.getResourceAsStream(MAP_DIR));
    }
    catch{}
    
    final RoadModel roadModel = new GraphRoadModel(graph);
    final CommunicationModel communicationModel = new CommunicationModel(rnd,
        false);
    sim.register(roadModel);
    sim.register(communicationModel);
    sim.configure();
    
 

    // add a number of drivers on the road
    for (int i = 0; i < NUM_AGENTS; i++) {
        final int radius = MIN_RADIUS + rnd.nextInt(MAX_RADIUS - MIN_RADIUS);
        final double speed = MIN_SPEED + (MAX_SPEED - MIN_SPEED)
            * rnd.nextDouble();
        final double reliability = MIN_RELIABILITY
            + (rnd.nextDouble() * (MAX_RELIABILITY - MIN_RELIABILITY));

        final Truck truck = new Truck(rnd, speed, radius,
            reliability);
        sim.register(truck);
      }
    
 // create GUI
    final UiSchema schema = new UiSchema(false);
    schema
        .add(ExamplePackage.class, "/graphics/perspective/deliverypackage2.png");

    final UiSchema schema2 = new UiSchema();
    schema2.add(Truck.C_BLACK, new RGB(0, 0, 0));
    schema2.add(Truck.C_YELLOW, new RGB(0xff, 0, 0));
    schema2.add(Truck.C_GREEN, new RGB(0x0, 0x80, 0));

    final View.Builder viewBuilder = View.create(sim)
        .with(new GraphRoadModelRenderer())
        .with(new RoadUserRenderer(schema, false))
        .with(new MessagingLayerRenderer(roadModel, schema2))
        .setSpeedUp(4);

    if (testing) {
      viewBuilder.enableAutoPlay()
          .enableAutoClose()
          .setSpeedUp(64)
          .stopSimulatorAtTime(60 * 60 * 1000);
    }

    viewBuilder.show();
  }
}

  

