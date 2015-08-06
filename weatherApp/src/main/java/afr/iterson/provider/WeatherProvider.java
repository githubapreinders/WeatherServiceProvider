package afr.iterson.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * Content Provider used to store Weather Data.
 */
public class WeatherProvider extends ContentProvider
{
	/**
	 * Logcat tag.
	 */
	private static final String TAG = WeatherProvider.class.getCanonicalName();

	/*
	 * Constants referencing the Contract class. Used for convenience.
	 */

	/**
	 * Constant for the Weather Data table's name.
	 */
	private static final String WEATHER_DATA_TABLE_NAME = WeatherContract.WeatherDataEntry.WEATHER_DATA_TABLE_NAME;

	/**
	 * Constant for the Weather Condition table's name
	 */
	private static final String WEATHER_CONDITION_TABLE_NAME = WeatherContract.WeatherConditionEntry.WEATHER_CONDITION_TABLE_NAME;

	/**
	 * UriMatcher code for the Weather Data table
	 */
	private static final int WEATHER_DATA_MATCHER_ID = 100;

	/**
	 * UriMatcher code for a specific row in the Weather Data table
	 */
	private static final int WEATHER_DATA_KEY_MATCHER_ID = 110;

	/**
	 * UriMatcher code for the Weather Condition table
	 */
	private static final int WEATHER_CONDITION_MATCHER_ID = 200;

	/**
	 * UriMatcher code for a specific row in the Weather Condition table.
	 */
	private static final int WEATHER_CONDITION_KEY_MATCHER_ID = 210;

	/**
	 * UriMatcher code for getting an entire "Weather Data" object's data from
	 * the database. This doesn't correspond to a specific table; it corresponds
	 * to a Weather Data entry and all of its associated Weather Condition
	 * entries.
	 */
	private static final int ACCESS_ALL_DATA_FOR_LOCATION_MATCHER_ID = 300;
	
	private static final int ACCESS_ALL_DATA_FOR_CITYIDS_MATCHER_ID = 310;


	/**
	 * Constant used to match a specific row id with the UriMatcher path.
	 */
	private static final String ROW_PATH_MOD = "/#";

	/**
	 * Build the UriMatcher for this Content Provider.
	 */
	private static UriMatcher buildUriMatcher()
	{
		// Add default 'no match' result to matcher.
		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

		// Store the authority.
		final String authority = WeatherContract.AUTHORITY;

		// Initialize the matcher with the URIs used to access each table
		matcher.addURI(authority, WEATHER_DATA_TABLE_NAME, WEATHER_DATA_MATCHER_ID);
		matcher.addURI(authority, WEATHER_DATA_TABLE_NAME + ROW_PATH_MOD, WEATHER_DATA_KEY_MATCHER_ID);

		// matcher.addURI(authority,
		// WEATHER_CONDITION_TABLE_NAME,
		// WEATHER_CONDITION_MATCHER_ID);
		//
		// matcher.addURI(authority,
		// WeatherContract.WeatherConditionEntry.WEATHER_CONDITION_TABLE_NAME
		// + ROW_PATH_MOD,
		// WEATHER_CONDITION_KEY_MATCHER_ID);

		matcher.addURI(authority, WeatherContract.ACCESS_ALL_DATA_FOR_LOCATION_PATH,
				ACCESS_ALL_DATA_FOR_LOCATION_MATCHER_ID);

		matcher.addURI(authority, WeatherContract.ACCESS_ALL_DATA_FOR_CITYIDS_PATH, 
				ACCESS_ALL_DATA_FOR_CITYIDS_MATCHER_ID);
		
		return matcher;
	}

	/**
	 * Constant defining the where clause modifier to use when referring to a
	 * specific row.
	 */
	private static final String SPECIFIC_ROW_MOD = " _id = ";

	/**
	 * 
	 * UriMatcher that is used to demultiplex the incoming URIs into requests.
	 */
	private static final UriMatcher sUriMatcher = buildUriMatcher();

	/**
	 * The database helper that is used to manage the providers db.
	 */
	private static WeatherDatabaseHelper mDBHelper;

	/**
	 * Hook method called when the provider is created.
	 */
	@Override
	public boolean onCreate()
	{
		mDBHelper = new WeatherDatabaseHelper(getContext());
		return true;
	}

	/**
	 * Helper method that appends a given key id to the end of the passed WHERE
	 * statement.
	 */
	private static String addKeyIdCheckToWhereStatement(String whereStatement, long id)
	{
		String newWhereStatement;
		if (TextUtils.isEmpty(whereStatement))
			newWhereStatement = "";
		else
			newWhereStatement = whereStatement + " AND ";

		return newWhereStatement + SPECIFIC_ROW_MOD + "'" + id + "'";
	}


	private static final String FROM_WHERE_STATEMENT_ALL_LOCATION_DATA = " FROM " + WEATHER_DATA_TABLE_NAME

	+ " WHERE " + WEATHER_DATA_TABLE_NAME + "." + WeatherContract.WeatherDataEntry.COLUMN_NAME + " = ? ";

	private static final String FROM_WHERE_STATEMENT_ALL_LOCATION_DATA__IDS = " FROM " + WEATHER_DATA_TABLE_NAME

	+ " WHERE "  + WeatherContract.WeatherDataEntry.COLUMN_CITYID + " = ? ";

	/**
	 * Get a Cursor containing all data for a selected location: It joins the
	 * Weather Data and Weather Condition tables. It will have a row for each
	 * Weather object corresponding to the location, with the Weather Data
	 * columns repeated.
	 */
	private Cursor getAllLocationsData(String location)
	{
		// Retreive the database from the helper
		final SQLiteDatabase db = mDBHelper.getWritableDatabase();

		// Query statement.
		final String selectQuery = "SELECT * " + FROM_WHERE_STATEMENT_ALL_LOCATION_DATA;

		Log.v(TAG, selectQuery);

		// Query the provider using the all-locations Uri, which will
		// return a Cursor joining the Weather Data and Conditions table
		// entries for one WeatherData object for the target location
		return db.rawQuery(selectQuery, new String[] { location });
	}

	private Cursor getAllLocationsDataForIds(String[] cityids)
	{
		// Retreive the database from the helper
		final SQLiteDatabase db = mDBHelper.getWritableDatabase();

		// Query statement.
		String selectQuery = "SELECT * " + FROM_WHERE_STATEMENT_ALL_LOCATION_DATA__IDS;


		if(cityids.length>1)
		{
			for(int i=1;i<cityids.length;i++)	
			selectQuery+= WeatherContract.WeatherDataEntry.OR_ADDITION_CITYIDS;
		}
		Log.v(TAG, selectQuery);
		// Query the provider using the all-locations Uri, which will
		// return a Cursor joining the Weather Data and Conditions table
		// entries for one WeatherData object for the target location
		return db.rawQuery(selectQuery, cityids);
	}

	
	public static Cursor getAllCityNames()
	{
		final SQLiteDatabase db = mDBHelper.getReadableDatabase();
		return db.rawQuery("select name from weather_data ", new String[] { "" });

	}

	/**
	 * Method called to handle query requests from client applications.
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String whereStatement, String[] whereStatementArgs,
			String sortOrder)
	{

		// Create a SQLite query builder that will be modified based
		// on the Uri passed.
		final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		// Use the passed Uri to determine how to build the
		// query. This will determine the table that the query will
		// act on and possibly add row qualifications to the WHERE
		// clause.
		switch (sUriMatcher.match(uri))
		{
		case WEATHER_DATA_MATCHER_ID:
			queryBuilder.setTables(WEATHER_DATA_TABLE_NAME);
			break;
		case WEATHER_DATA_KEY_MATCHER_ID:
			queryBuilder.setTables(WEATHER_DATA_TABLE_NAME);
			whereStatement = addKeyIdCheckToWhereStatement(whereStatement, ContentUris.parseId(uri));
			break;
		case ACCESS_ALL_DATA_FOR_LOCATION_MATCHER_ID:
			// This is a special Uri that is querying for an entire
			// Weather Data object
			return getAllLocationsData(whereStatementArgs[0]);
			
		case ACCESS_ALL_DATA_FOR_CITYIDS_MATCHER_ID : 

			return getAllLocationsDataForIds(whereStatementArgs);
			
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// One the query builder has been initialized based on the provided
		// Uri, use it to query the database
		final Cursor cursor = queryBuilder.query(mDBHelper.getWritableDatabase(), projection, whereStatement,
				whereStatementArgs, null, // GROUP BY (not used)
				null, // HAVING (not used)
				sortOrder);

		// Register to watch a content URI for changes.
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	/**
	 * Method called to handle type requests from client applications. It
	 * returns the MIME type of the data associated with each URI.
	 */
	@Override
	public String getType(Uri uri)
	{
		// Use the passed Uri to determine what data is being asked
		// for and return the appropriate MIME type
		switch (sUriMatcher.match(uri))
		{
		
		case WEATHER_DATA_KEY_MATCHER_ID:
			return WeatherContract.WeatherDataEntry.WEATHER_DATA_ITEM_CONTENT_TYPE;
			// case WEATHER_CONDITION_MATCHER_ID:
			// return
			// WeatherContract.WeatherConditionEntry.WEATHER_CONDITION_CONTENT_TYPE;
			// case WEATHER_CONDITION_KEY_MATCHER_ID:
			// return
			// WeatherContract.WeatherConditionEntry.WEATHER_CONDITION_ITEM_CONTENT_TYPE;
		case ACCESS_ALL_DATA_FOR_LOCATION_MATCHER_ID:
			return WeatherContract.ACCESS_ALL_DATA_FOR_LOCATION_CONTENT_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	/**
	 * Method called to handle insert requests from client applications.
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values)
	{
		// The table to perform the insert on
		String table;

		// The Uri containing the inserted row's id that is
		// returned to the caller
		Uri resultUri;

		// Determine the base Uri to return and the table to insert on
		// using the UriMatcher.
		switch (sUriMatcher.match(uri))
		{
		case WEATHER_DATA_MATCHER_ID:
			table = WEATHER_DATA_TABLE_NAME;
			resultUri = WeatherContract.WeatherDataEntry.WEATHER_DATA_CONTENT_URI;
			break;

		// case WEATHER_CONDITION_MATCHER_ID:
		// table = WEATHER_CONDITION_TABLE_NAME;
		// resultUri =
		// WeatherContract.WeatherConditionEntry.WEATHER_CONDITION_CONTENT_URI;
		// break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		// Insert the data into the correct table
		final long insertRow = mDBHelper.getWritableDatabase().insert(table, "", values);
		// Check to ensure that the insertion worked.
		if (insertRow > 0)
		{
			// Create the result URI.
			Uri newUri = ContentUris.withAppendedId(resultUri, insertRow);

			// Register to watch a content URI for changes.
			getContext().getContentResolver().notifyChange(newUri, null);
			return newUri;
		} else
			throw new SQLException("Fail to add a new record into " + uri);
	}

	/**
	 * Method called to handle delete requests from client applications.
	 */
	@Override
	public int delete(Uri uri, String whereStatement, String[] whereStatementArgs)
	{
		// Number of rows deleted.
		int rowsDeleted;

		final SQLiteDatabase db = mDBHelper.getWritableDatabase();

		// Delete the appropriate rows based on the Uri. If the URI
		// includes a specific row to delete, add that row to the
		// WHERE statement.
		switch (sUriMatcher.match(uri))
		{
		case WEATHER_DATA_MATCHER_ID:
			rowsDeleted = db.delete(WEATHER_DATA_TABLE_NAME, whereStatement, whereStatementArgs);
			break;
		case WEATHER_DATA_KEY_MATCHER_ID:
			whereStatement = addKeyIdCheckToWhereStatement(whereStatement, ContentUris.parseId(uri));
			rowsDeleted = db.delete(WEATHER_DATA_TABLE_NAME, whereStatement, whereStatementArgs);
			break;
		// case WEATHER_CONDITION_MATCHER_ID:
		// rowsDeleted =
		// db.delete(WEATHER_CONDITION_TABLE_NAME,
		// whereStatement,
		// whereStatementArgs);
		// break;
		// case WEATHER_CONDITION_KEY_MATCHER_ID:
		// whereStatement =
		// addKeyIdCheckToWhereStatement(whereStatement,
		// ContentUris.parseId(uri));
		// rowsDeleted =
		// db.delete(WEATHER_CONDITION_TABLE_NAME,
		// whereStatement,
		// whereStatementArgs);
		// break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		// Register to watch a content URI for changes.
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}

	/**
	 * Method called to handle update requests from client applications.
	 */
	@Override
	public int update(Uri uri, ContentValues values, String whereStatement, String[] whereStatementArgs)
	{
		// Number of rows updated.
		int rowsUpdated;

		final SQLiteDatabase db = mDBHelper.getWritableDatabase();

		// Update the appropriate rows. If the URI includes a specific
		// row to update, add that row to the where statement.
		switch (sUriMatcher.match(uri))
		{
		case WEATHER_DATA_MATCHER_ID:
			rowsUpdated = db.update(WEATHER_DATA_TABLE_NAME, values, whereStatement, whereStatementArgs);
			break;
		case WEATHER_DATA_KEY_MATCHER_ID:
			whereStatement = addKeyIdCheckToWhereStatement(whereStatement, ContentUris.parseId(uri));
			rowsUpdated = db.update(WEATHER_DATA_TABLE_NAME, values, whereStatement, whereStatementArgs);
			break;
		case WEATHER_CONDITION_MATCHER_ID:
			rowsUpdated = db.update(WEATHER_CONDITION_TABLE_NAME, values, whereStatement, whereStatementArgs);
			break;
		case WEATHER_CONDITION_KEY_MATCHER_ID:
			whereStatement = addKeyIdCheckToWhereStatement(whereStatement, ContentUris.parseId(uri));
			rowsUpdated = db.update(WEATHER_CONDITION_TABLE_NAME, values, whereStatement, whereStatementArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// Register to watch a content URI for changes.
		getContext().getContentResolver().notifyChange(uri, null);

		return rowsUpdated;
	}

	/**
	 * Method that handles bulk insert requests.
	 */
	@Override
	public int bulkInsert(Uri uri, ContentValues[] values)
	{

		// fetch the db from the helper
		final SQLiteDatabase db = mDBHelper.getWritableDatabase();

		String dbName;

		// Match the Uri against the table's uris to determine
		// the table in which table to insert the values
		switch (sUriMatcher.match(uri))
		{
		case WEATHER_DATA_MATCHER_ID:
			dbName = WeatherContract.WeatherDataEntry.WEATHER_DATA_TABLE_NAME;
			break;
		// case WEATHER_CONDITION_MATCHER_ID :
		// dbName =
		// WeatherContract.WeatherConditionEntry.WEATHER_CONDITION_TABLE_NAME;
		// break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// Insert the values into the table in one transaction

		// Begins a transaction in EXCLUSIVE mode.
		db.beginTransaction();
		int returnCount = 0;
		try
		{
			for (ContentValues value : values)
			{
				final long id = db.insert(dbName, null, value);
				if (id != -1)
					returnCount++;
			}
			// Marks the current transaction as successful.
			db.setTransactionSuccessful();

		} finally
		{
			// End the transaction
			db.endTransaction();
		}

		// Notifies registered observers that rows were updated
		// and attempt to sync changes to the network.
		getContext().getContentResolver().notifyChange(uri, null);
		return returnCount;
	}
}
