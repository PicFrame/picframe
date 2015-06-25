package picframe.at.picframe.helper.viewpager;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import java.io.IOException;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by ClemensH on 06.04.2015.
 */
public class EXIF_helper {
    private ExifInterface reader = null;

    public static Bitmap decodeFile(String filePath, Context myContext) {

        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, o);

        // The new size we want to scale to ( max display resolution)
        WindowManager wm = (WindowManager) myContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        int REQUIRED_SIZE = 2048;
        REQUIRED_SIZE = width>height? width : height;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth;
        int height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp <= REQUIRED_SIZE && height_tmp <= REQUIRED_SIZE)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }
        //System.out.println("Picture width: " + width_tmp + " --- Pic height: " + height_tmp + "\nScaleFactor: " +scale);

        // Decode with correct scale (inSampleSize)
        o = new BitmapFactory.Options();
        o.inSampleSize = scale;
        Bitmap b1 = BitmapFactory.decodeFile(filePath, o);
        // Rotate scaled image according to EXIF Information stored in the file
        Bitmap b = EXIF_helper.rotateBitmap(filePath, b1);

        return b;
        // image.setImageBitmap(bitmap);
    }

// @see http://sylvana.net/jpegcrop/exif_orientation.html
    public static Bitmap rotateBitmap(String src, Bitmap bitmap) {
        try {
            int orientation = getExifOrientation(src);
            if (orientation == 1) {
                return bitmap;
            }

            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.setScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.setRotate(180);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(-90);
                    break;
                default:
                    return bitmap;
            }

            try {
                Bitmap oriented = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                bitmap.recycle();
                return oriented;
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                return bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    private static int getExifOrientation(String src) throws IOException {
        int orientation = ExifInterface.ORIENTATION_NORMAL;

        try {
          ExifInterface exif = new ExifInterface(src);
          orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return orientation;
    }

    /*
    	// Change color scale of bitmap to grayscale
	public Bitmap ConvertToGrayscale(Bitmap sampleBitmap) {
		final ColorMatrix gsMatrix = new ColorMatrix();
		gsMatrix.setSaturation(0);
		final ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(
				gsMatrix);
		sampleBitmap = sampleBitmap.copy(Bitmap.Config.ARGB_8888, true);
		final Paint paint = new Paint();
		paint.setColorFilter(colorFilter);
		final Canvas myCanvas = new Canvas(sampleBitmap);
		myCanvas.drawBitmap(sampleBitmap, 0, 0, paint);
		Log.i(LOG_PROV, LOG_NAME
				+ "Changed Bitmap to grayscale in ConvertToGrayScale");
		return sampleBitmap;
	}

	// Change color scale of bitmap to sepia
	public Bitmap ConvertToSepia(Bitmap sampleBitmap) {
		final ColorMatrix sepiaMatrix = new ColorMatrix();
		final float[] sepMat = { 0.3930000066757202f, 0.7689999938011169f,
				0.1889999955892563f, 0, 0, 0.3490000069141388f,
				0.6859999895095825f, 0.1679999977350235f, 0, 0,
				0.2720000147819519f, 0.5339999794960022f, 0.1309999972581863f,
				0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1 };
		sepiaMatrix.set(sepMat);
		final ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(
				sepiaMatrix);
		sampleBitmap = sampleBitmap.copy(Bitmap.Config.ARGB_8888, true);
		final Paint paint = new Paint();
		paint.setColorFilter(colorFilter);
		final Canvas myCanvas = new Canvas(sampleBitmap);
		myCanvas.drawBitmap(sampleBitmap, 0, 0, paint);
		Log.i(LOG_PROV, LOG_NAME + "Changed Bitmap to sepia in ConvertToSepia");
		return sampleBitmap;
	}
    * */
}
