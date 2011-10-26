package greendroid.widget;

import java.util.ArrayList;
import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.content.Context;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class BasicItemizedOverlay extends ItemizedOverlay<OverlayItem> {
  private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
  private Context mContext;

  public BasicItemizedOverlay(Context context, Drawable defaultMarker) {
      super(boundCenterBottom(defaultMarker));

      mContext = context;
  }

  public void addOverlay(OverlayItem overlay) {
      mOverlays.add(overlay);
      populate();
  }

  @Override
  protected OverlayItem createItem(int i) {
      return mOverlays.get(i);
  }

  @Override
  public int size() {
      return mOverlays.size();
  }

  @Override
  protected boolean onTap(int index) {
    OverlayItem item = mOverlays.get(index);
    AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
    dialog.setTitle(item.getTitle());
    dialog.setMessage(item.getSnippet());
    dialog.show();
    return true;
  }

}
