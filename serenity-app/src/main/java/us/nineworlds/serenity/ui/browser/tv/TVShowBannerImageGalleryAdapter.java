/**
 * The MIT License (MIT)
 * Copyright (c) 2013 David Carver
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF
 * OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package us.nineworlds.serenity.ui.browser.tv;

import java.util.ArrayList;
import java.util.List;

import us.nineworlds.serenity.SerenityApplication;
import us.nineworlds.serenity.core.model.SeriesContentInfo;
import us.nineworlds.serenity.core.model.impl.AbstractSeriesContentInfo;
import us.nineworlds.serenity.core.model.impl.TVShowSeriesInfo;
import us.nineworlds.serenity.core.services.ShowRetrievalIntentService;
import us.nineworlds.serenity.ui.adapters.AbstractPosterImageGalleryAdapter;
import us.nineworlds.serenity.ui.util.ImageUtils;
import us.nineworlds.serenity.ui.views.TVShowImageView;

import us.nineworlds.serenity.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.TextView;

/**
 * 
 * @author dcarver
 * 
 */
public class TVShowBannerImageGalleryAdapter extends
		AbstractPosterImageGalleryAdapter {

	/**
	 * 
	 */
	private static final int BANNER_PIXEL_HEIGHT = 140;

	/**
	 * 
	 */
	private static final int BANNER_PIXEL_WIDTH = 758;

	private static List<TVShowSeriesInfo> tvShowList = null;

	private String key;
	private static ProgressDialog pd;

	public TVShowBannerImageGalleryAdapter(Context c, String key,
			String category) {
		super(c, key, category);
		tvShowList = new ArrayList<TVShowSeriesInfo>();

		imageLoader = SerenityApplication.getImageLoader();
		this.key = key;

		try {
			fetchData();
		} catch (Exception ex) {
			Log.e(getClass().getName(), "Error connecting to plex.", ex);
		}
	}

	protected void fetchData() {
		pd = ProgressDialog.show(context, "", "Retrieving Shows.");
		handler = new ShowRetrievalHandler();
		Messenger messenger = new Messenger(handler);
		Intent intent = new Intent(context, ShowRetrievalIntentService.class);
		intent.putExtra("MESSENGER", messenger);
		intent.putExtra("key", key);
		intent.putExtra("category", category);
		context.startService(intent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		return tvShowList.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		return tvShowList.get(position);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getView(int, android.view.View,
	 * android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View galleryCellView = context.getLayoutInflater().inflate(R.layout.poster_tvshow_indicator_view, null);
		
		SeriesContentInfo pi = tvShowList.get(position);
		TVShowImageView mpiv = (TVShowImageView) galleryCellView.findViewById(R.id.posterImageView);
		mpiv.setPosterInfo(pi);
		mpiv.setBackgroundResource(R.drawable.gallery_item_background);
		mpiv.setScaleType(ImageView.ScaleType.FIT_XY);
		int width = ImageUtils.getDPI(BANNER_PIXEL_WIDTH, context);
		int height = ImageUtils.getDPI(BANNER_PIXEL_HEIGHT, context);
		mpiv.setLayoutParams(new RelativeLayout.LayoutParams(width, height));

		imageLoader.displayImage(pi.getImageURL(), mpiv);
		galleryCellView.setLayoutParams(new Gallery.LayoutParams(width, height));
		
		toggleWatchedIndicator(galleryCellView, pi);
		
		return galleryCellView;
	}

	/**
	 * @param galleryCellView
	 * @param pi
	 */
	protected void toggleWatchedIndicator(View galleryCellView,
			SeriesContentInfo pi) {
		int unwatched = 0;
				
		if (pi.getShowsUnwatched() != null) {
			unwatched = Integer.parseInt(pi.getShowsUnwatched());
		}
		
		ImageView watchedView = (ImageView) galleryCellView.findViewById(R.id.posterWatchedIndicator);
		if (unwatched == 0) {
			watchedView.setImageResource(R.drawable.overlaywatched);
		}
		
		int watched = 0;
		if (pi.getShowsWatched() != null ) {
			watched = Integer.parseInt(pi.getShowsWatched());
		}
		
		int totalShows = unwatched + watched;
		if (unwatched != totalShows) {
			toggleProgressIndicator(galleryCellView, watched, totalShows, watchedView);
		}
		
		
	}
	

	private class ShowRetrievalHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {

			tvShowList = (List<TVShowSeriesInfo>) msg.obj;
			Gallery posterGallery = (Gallery) context
					.findViewById(R.id.tvShowBannerGallery);
			if (tvShowList != null) {
				TextView tv = (TextView) context
						.findViewById(R.id.tvShowItemCount);
				if (tv == null) {
					pd.dismiss();
					return;
				}
				if (tvShowList.isEmpty()) {
					Toast.makeText(context, context.getString(R.string.no_shows_found_for_the_category_) + category, Toast.LENGTH_LONG).show();
				}
				tv.setText(Integer.toString(tvShowList.size()) + context.getString(R.string._item_s_));
			}
			notifyDataSetChanged();
			posterGallery.requestFocus();
			pd.dismiss();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.github.kingargyle.plexappclient.ui.adapters.
	 * AbstractPosterImageGalleryAdapter#fetchDataFromService()
	 */
	@Override
	protected void fetchDataFromService() {
		// TODO Auto-generated method stub

	}

}
