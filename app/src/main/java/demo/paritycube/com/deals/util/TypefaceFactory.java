package demo.paritycube.com.deals.util;

import android.content.Context;
import android.graphics.Typeface;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import demo.paritycube.com.deals.R;


public class TypefaceFactory
{
  /* Constants */

  public static final int RobotoBold = 0;
  public static final int RobotoBoldItalic = 1;
  public static final int RobotoItalic = 2;
  public static final int RobotoLight = 3;
  public static final int RobotoLightItalic = 4;
  public static final int RobotoMedium = 5;
  public static final int RobotoMediumItalic = 6;
  public static final int RobotoRegular = 7;
  public static final int RobotoThin = 8;

  /* Static properties */

  private static Map<String, Typeface> sm_typefaceCache
      = new HashMap<String, Typeface>();
  private static Map<String, String> sm_typefacePathCache
      = new HashMap<String, String>();

  private static int[] RawRes = {
    R.raw.font_roboto_bold,
    R.raw.font_roboto_bolditalic,
    R.raw.font_roboto_italic,
    R.raw.font_roboto_light,
    R.raw.font_roboto_lightitalic,
    R.raw.font_roboto_medium,
    R.raw.font_roboto_mediumitalic,
    R.raw.font_roboto_regular,
    R.raw.font_roboto_thin,
  };

  /* Static methods */

  /**
   * Returns a typeFace from a pre-defined constant.
   *
   * @param context
   *          the Context reference to create the TypeFace with
   * @param typefaceID
   *          the integer constant/ID of the typeFace
   * @return the typeFace instance.
   */
  public static Typeface getTypeFaceForId (Context context, int typefaceID)
  {
    return getTypefaceForRawResId (context, RawRes[typefaceID]);
  }

  /**
   * Returns the typeFace file path from the pre-defined constant.
   *
   * @param context
   *          the Context reference to create the TypeFace with
   * @param typefaceID
   *          the integer constant/ID of the typeFace
   * @return the typeFace file path.
   */
  public static String getTypeFacePathForId (Context context, int typefaceID)
  {
    Map<String, String> typefacePathCache = sm_typefacePathCache;

    int typefaceRawResID = RawRes[typefaceID];
    String key = context.getResources().getResourceEntryName(typefaceRawResID);

    String typefacePath = typefacePathCache.get(key);
    if (typefacePath == null)
    {
      getTypefaceForRawResId (context, typefaceRawResID);
    }
    typefacePath = typefacePathCache.get(key);

    return typefacePath;
  }

  /**
   * Returns a typeface from a raw resource ID.
   *
   * @param context
   *          the Context reference to create the TypeFace with
   * @param typefaceRawResID
   *          the integer raw resource ID of the typeface
   * @return the Typeface instance
   */
  public static Typeface getTypefaceForRawResId (Context context,
      int typefaceRawResID)
  {
    Map<String, Typeface> typefaceCache = sm_typefaceCache;
    Map<String, String> typefacePathCache = sm_typefacePathCache;

    String key = context.getResources().getResourceEntryName(typefaceRawResID);

    Typeface typeface = typefaceCache.get(key);
    if (typeface == null)
    {
      File file = new File(getDirectory(context) + "/rawfont-" + key + ".ttf");
      if (!file.exists())
      {
        InputStream is = null;
        BufferedOutputStream out = null;
        try
        {
          is = context.getResources().openRawResource(typefaceRawResID);
          out = new BufferedOutputStream(new FileOutputStream(file));

          int len = 0;
          byte[] buffer = new byte[is.available()];
          while((len = is.read(buffer)) > 0)
          {
            out.write(buffer, 0, len);
          }
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
        finally
        {
          try
          {
            is.close();
            out.close();
          }
          catch (IOException e)
          {
            e.printStackTrace();
          }
        }
      }

      typeface = Typeface.createFromFile(file);

      typefaceCache.put(key, typeface);
      typefacePathCache.put(key, file.getPath());
    }
    return typeface;
  }

  /**
   * Returns the String file directory where the raw font will be stored.
   */
  private static String getDirectory (Context context)
  {
    return context.getCacheDir().getPath();
  }
}
