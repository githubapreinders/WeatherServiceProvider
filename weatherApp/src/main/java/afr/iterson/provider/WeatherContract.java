package afr.iterson.provider;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * This contract defines the metadata for the Weather Content Provider,
 * including the provider's access URIs and its database constants.
 */
public class WeatherContract
{
	/**
	 * The Weather Provider's unique identifier.
	 */
	public static final String AUTHORITY = "afr.iterson.WeatherProvider";

	/**
	 * The base of all URIs that are used to communicate with the provider.
	 */
	private static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

	/**
	 * Constant for directory MIME types.
	 */
	private static final String MIME_TYPE_DIR = "vnd.android.cursor.dir/";

	/**
	 * Constant for single item MIME types.
	 */
	private static final String MIME_TYPE_ITEM = "vnd.android.cursor.item/";

	/**
	 * Path used to access all data for a given location.
	 */
	public static final String ACCESS_ALL_DATA_FOR_LOCATION_PATH = "access_all_for_location";

	public static final String ACCESS_ALL_DATA_FOR_CITYIDS_PATH = "access_all_for_cityids";

	/**
	 * URI used to access all the data for a given location. This will access a
	 * cursor that concatenates the row of the Weather Data entry corresponding
	 * to the location with all the rows of its associated Weather Condition
	 * entries
	 */
	public static final Uri ACCESS_ALL_DATA_FOR_LOCATION_URI = BASE_URI.buildUpon()
			.appendPath(ACCESS_ALL_DATA_FOR_LOCATION_PATH).build();

	public static final Uri ACCESS_ALL_DATA_FOR_CITYIDS = BASE_URI.buildUpon()
			.appendPath(ACCESS_ALL_DATA_FOR_CITYIDS_PATH).build();

	/**
	 * MIME type for accessing all the data for a location.
	 */
	public static final String ACCESS_ALL_DATA_FOR_LOCATION_CONTENT_TYPE = MIME_TYPE_DIR + AUTHORITY + "/"
			+ ACCESS_ALL_DATA_FOR_LOCATION_PATH;

	/**
	 * Inner class defining the contents of the Weather Data table.
	 */
	public static final class WeatherDataEntry implements BaseColumns
	{
		/**
		 * Weather Data's Table name.
		 */
		public static String WEATHER_DATA_TABLE_NAME = "weather_data";

		/**
		 * Unique URI for the Weather Data table.
		 */
		public static final Uri WEATHER_DATA_CONTENT_URI = BASE_URI.buildUpon().appendPath(WEATHER_DATA_TABLE_NAME)
				.build();

		/**
		 * MIME type for multiple Weather Data rows.
		 */
		public static final String WEATHER_DATA_CONTENT_TYPE = MIME_TYPE_DIR + AUTHORITY + "/"
				+ WEATHER_DATA_TABLE_NAME;

		/**
		 * MIME type for a single Weather Data row
		 */
		public static final String WEATHER_DATA_ITEM_CONTENT_TYPE = MIME_TYPE_ITEM + AUTHORITY + "/"
				+ WEATHER_DATA_TABLE_NAME;

		/*
		 * Weather Data Table's Columns
		 */
		public static final String COLUMN_NAME = "name";
		public static final String COLUMN_DATE = "date";
		public static final String COLUMN_COD = "cod";

		public static final String COLUMN_SUNRISE = "sunrise";
		public static final String COLUMN_SUNSET = "sunset";
		public static final String COLUMN_COUNTRY = "country";

		public static final String COLUMN_TEMP = "temp";
		public static final String COLUMN_HUMIDITY = "humidity";
		public static final String COLUMN_PRESSURE = "pressure";

		public static final String COLUMN_SPEED = "speed";
		public static final String COLUMN_DEG = "deg";

		public static final String COLUMN_ICON1 = "icon1";
		public static final String COLUMN_ICON2 = "icon2";
		public static final String COLUMN_ICON3 = "icon3";
		public static final String COLUMN_ICON4 = "icon4";
		public static final String COLUMN_ICON5 = "icon5";

		public static final String COLUMN_CITYID = "cityid";

		public static final String COLUMN_SUNRISESTRING = "sunrisestring";
		public static final String COLUMN_SUNSETSTRING = "sunsetstring";
		public static final String COLUMN_DATESTRING = "datestring";
		public static final String COLUMN_GMTOFFSET = "gmtoffset";

		public static final String COLUMN_MAINICON = "mainicon";

		public static final String COLUMN_LONGITUDE = "longitude";
		public static final String COLUMN_LATTITUDE = "lattitude";

		public static final String COLUMN_EXPIRATION_TIME = "expiration_time";

		/**
		 * Return a URI that points to the row containing the given ID.
		 */
		public static Uri buildRowAccessUri(Long id)
		{
			return ContentUris.withAppendedId(WEATHER_DATA_CONTENT_URI, id);
		}

		public static String OR_ADDITION_CITYIDS = "OR " + WeatherDataEntry.COLUMN_CITYID + "=?";

	}

	/**
	 * Inner class defining the contents of the Weather Condition table.
	 */
	public static final class WeatherConditionEntry implements BaseColumns
	{
		/**
		 * Weather Condition's Table name.
		 */
		public static String WEATHER_CONDITION_TABLE_NAME = "weather_condition";

		public static final String COLUMN_DESCRIPTION = "description";
		
		
		/**
		 * Unique URI for the Weather Condition table.
		 */
		public static final Uri WEATHER_CONDITION_CONTENT_URI = BASE_URI.buildUpon()
				.appendPath(WEATHER_CONDITION_TABLE_NAME).build();

	}
}
