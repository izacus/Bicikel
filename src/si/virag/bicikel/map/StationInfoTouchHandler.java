package si.virag.bicikel.map;

import si.virag.bicikel.R;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class StationInfoTouchHandler implements OnTouchListener 
{
	private Context context;
	private Drawable defaultBackground;
	private Drawable selectedBackground;
	
	public StationInfoTouchHandler(Context context)
	{
		this.context = context;
		this.selectedBackground = context.getResources().getDrawable(R.drawable.info_select);
	}
	
	@Override
	public boolean onTouch(View parent, MotionEvent motionEvent) 
	{
		switch(motionEvent.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				if (defaultBackground == null)
					defaultBackground = parent.getBackground();
				
				parent.setBackgroundDrawable(selectedBackground);
				break;
			case MotionEvent.ACTION_UP:
				parent.setBackgroundDrawable(defaultBackground);
				break;
			default:
				break;
		}
		
		return true;
	}

}
