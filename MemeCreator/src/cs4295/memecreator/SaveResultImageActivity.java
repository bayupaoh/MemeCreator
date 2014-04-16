package cs4295.memecreator;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class SaveResultImageActivity extends Activity {
	private ImageView resultImage;
	private Bitmap tempImage;
	final Context context = this;
	private SaveResultImageActivity selfRef = this;
	private SharedPreferences setting;
	private boolean saveAndShare = false;
	private boolean shareButtonPressed = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_save_result_image);

		// Set the actioin bar style
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color
				.parseColor("#503C3C3C")));
		actionBar.setIcon(R.drawable.back_icon);
		actionBar.setHomeButtonEnabled(true);

		// Transparent bar on android 4.4 or above
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			Window window = getWindow();
			// Translucent status bar
			window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			// Translucent navigation bar
			window.setFlags(
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}

		// Get the intent and set the image path to be the result image
		Intent shareIntent = getIntent();
		String imagePath = shareIntent
				.getStringExtra("cs4295.memcreator.imagePath");

		// Uri memeBitmapUri =
		// shareIntent.getParcelableExtra("cs4295.memcreator.memeBitmapURI");
		// Bundle extras = shareIntent.getExtras();
		byte[] memeBitmapByteArray = getIntent().getByteArrayExtra(
				"cs4295.memcreator.memeBitmapByteArray");
		Bitmap memeBitmap = BitmapFactory.decodeByteArray(memeBitmapByteArray,
				0, memeBitmapByteArray.length);

		// Set result image
		resultImage = (ImageView) this.findViewById(R.id.resultImage);
		// if(memeBitmap!=null){
		// Log.i("memeBitmapUri",memeBitmapUri.getPath());
		resultImage.setImageBitmap(memeBitmap);
		// }
		// else
		// resultImage.setImageBitmap(BitmapFactory.decodeFile(imagePath));
		resultImage.setDrawingCacheEnabled(true);
		resultImage.buildDrawingCache();

		tempImage = ((BitmapDrawable) resultImage.getDrawable()).getBitmap();

		setting = PreferenceManager.getDefaultSharedPreferences(this);

		// Share button on click
		Button share = (Button) findViewById(R.id.shareButton);
		share.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				saveAndShare = setting.getBoolean("example_checkbox", false);

				if (saveAndShare)
					saveImageHelper();
				// Build the intent
				shareButtonPressed = true;
			}
		});

		// Save button on click
		Button save = (Button) findViewById(R.id.saveButton);
		save.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				saveImageHelper();
			}
		});
	}
	
	private void shareHelper()
	{
		Uri uriToImage = Uri
				.parse(android.provider.MediaStore.Images.Media
						.insertImage(SaveResultImageActivity.this
								.getContentResolver(), tempImage, null,
								null));
		Intent imageIntent = new Intent(Intent.ACTION_SEND);
		imageIntent.setType("image/*");
		imageIntent.putExtra(Intent.EXTRA_STREAM, uriToImage);

		// Verify it resolves
		PackageManager packageManager = getPackageManager();
		List<ResolveInfo> activities = packageManager
				.queryIntentActivities(imageIntent, 0);
		boolean isIntentSafe = activities.size() > 0;

		// Start an activity if it's safe
		if (isIntentSafe) {
			startActivity(Intent.createChooser(imageIntent,
					"Share images to.."));
		} else {
			show();
		}
	}

	private void saveImageHelper() {
		// save Image in Internal with own Folder
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		// set title
		builder.setTitle("Save Image");

		// set default input value
		final EditText input = new EditText(context);
		File direct = new File("/sdcard/DCIM/Meme/Media/");
		int number = countImageNo(direct) + 1;
		input.setText("MemeImage " + number);

		// set dialog message
		builder.setMessage("Input Image Name")
				.setCancelable(true)
				.setView(input)
				.setPositiveButton("Save",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// if this button is clicked, close
								// current activity
								saveImage(tempImage, input.getText() + ".png");
								if(shareButtonPressed)
									shareHelper();
								else
									finish();
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// if this button is clicked, just close
								// the dialog box and do nothing
								dialog.cancel();
							}
						});

		// create alert dialog
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}

	// Method to count the number of Image inside the file
	private int countImageNo(File dir) {
		// TODO Auto-generated method stub
		try {
			File[] files = dir.listFiles();
			Log.i("File Number", " " + files.length);
			return files.length;
		} catch (Exception e) {

		}
		return 0;
	}

	// Method to show notification when sharing is failed
	private void show() {
		Toast.makeText(this, "Sorry, share failed", 2000).show();
	}

	// Method to save the image
	private void saveImage(Bitmap image, String fileName) {

		// http://developer.android.com/guide/topics/data/data-storage.html#filesExternal
		// File direct = new File(Environment.getExternalStoragePublicDirectory(
		// Environment.DIRECTORY_PICTURES), "Hi");

		File direct = new File("/sdcard/DCIM/Meme/Media/");

		if (!direct.exists()) {
			direct.mkdirs();
		}

		File file = new File(new File("/sdcard/DCIM/Meme/Media/"), fileName);
		if (file.exists())
			file.delete();
		try {
			FileOutputStream out = new FileOutputStream(file);
			image.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();

			Toast.makeText(this,
					fileName + " is saved at /sdcard/DCIM/Meme/Media/", 2000)
					.show();

			// update the save image to gallery
			MediaScannerConnectionClient client = new MyMediaScannerConnectionClient(
					getApplicationContext(), file, null);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// an instance of MediaScannerConnection(use for update gallery )
	final class MyMediaScannerConnectionClient implements
			MediaScannerConnectionClient {

		private String mFilename;
		private String mMimetype;
		private MediaScannerConnection mConn;

		public MyMediaScannerConnectionClient(Context ctx, File file,
				String mimetype) {
			this.mFilename = file.getAbsolutePath();
			mConn = new MediaScannerConnection(ctx, this);
			mConn.connect();
		}

		@Override
		public void onMediaScannerConnected() {
			mConn.scanFile(mFilename, mMimetype);
		}

		@Override
		public void onScanCompleted(String path, Uri uri) {
			mConn.disconnect();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.save_result_image, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent intent = new Intent(selfRef, SettingsActivity.class);
			startActivity(intent);

			return true;
		case android.R.id.home:
			// When the action bar icon on the top right is clicked, finish this
			// activity
			this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
