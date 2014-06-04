package mas.ui;

import java.util.Set;

import javax.annotation.Nullable;

import mas.BDIParcel;
import mas.BDIVehicle;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

import rinde.sim.core.graph.Point;
import rinde.sim.core.model.ModelProvider;
import rinde.sim.core.model.communication.CommunicationUser;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.RoadUser;
import rinde.sim.ui.renderers.ModelRenderer;
import rinde.sim.ui.renderers.UiSchema;
import rinde.sim.ui.renderers.ViewPort;
import rinde.sim.ui.renderers.ViewRect;

public class MessagingLayerRenderer implements ModelRenderer {

	protected RoadModel rs;
	private final UiSchema uiSchema;

	public static final String RADIUS_COLOR = "radius_color";

	public MessagingLayerRenderer(UiSchema uiSchema) {
		this.uiSchema = uiSchema;
	}

	@Override
	public void renderDynamic(GC gc, ViewPort vp, long time) {
		uiSchema.initialize(gc.getDevice());

		final Set<BDIVehicle> vehicles = rs.getObjectsOfType(BDIVehicle.class);
		synchronized (vehicles) {
			for (final BDIVehicle vehicle : vehicles) {
				renderObject(gc, vp, vehicle);
			}
		}

		final Set<BDIParcel> parcels = rs.getObjectsOfType(BDIParcel.class);
		synchronized (parcels) {
			for (final BDIParcel parcel : parcels) {
				renderObject(gc, vp, parcel);
			}
		}
	}

	private <T extends RoadUser & CommunicationUser> void renderObject(GC gc,
			ViewPort vp, T a) {
		Point p = a.getPosition();
		if (p == null) {
			return;
		}

		final int x = (int) (vp.origin.x + (p.x - vp.rect.min.x) * vp.scale);
		final int y = (int) (vp.origin.y + (p.y - vp.rect.min.y) * vp.scale);

		final int radius = (int) (a.getRadius() * vp.scale);

		Color c = uiSchema.getColor(RADIUS_COLOR);
		gc.setForeground(c);
		gc.setBackground(c);

		gc.setAlpha(25);
		gc.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
		gc.setAlpha(255);
	}

	@Override
	public void renderStatic(GC gc, ViewPort vp) {
	}

	@Nullable
	@Override
	public ViewRect getViewRect() {
		return null;
	}

	@Override
	public void registerModelProvider(ModelProvider mp) {
		rs = mp.getModel(RoadModel.class);
	}

}
