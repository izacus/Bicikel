package si.virag.bicikel.map;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class IconOverlay extends Overlay
{
	private int icon;
	private GeoPoint pt;
	private Resources res;
	
	public IconOverlay(Resources res, GeoPoint pt, int iconResId)
	{
		this.res = res;
		this.pt = pt;
		this.icon = iconResId;
	}
	
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow)
	{
		super.draw(canvas, mapView, shadow);
		
		Point screenPts = new Point();
		mapView.getProjection().toPixels(pt, screenPts);
		
		Bitmap image = BitmapFactory.decodeResource(res, icon);
		canvas.drawBitmap(image, screenPts.x - 16, screenPts.y - 37, null);
	}


	@Override
	public boolean onTap(GeoPoint p, MapView mapView)
	{
		// TODO Auto-generated method stub
		return super.onTap(p, mapView);
	}
}
