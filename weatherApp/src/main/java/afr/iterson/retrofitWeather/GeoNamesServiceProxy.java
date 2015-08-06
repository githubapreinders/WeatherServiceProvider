package afr.iterson.retrofitWeather;

import retrofit.http.GET;
import retrofit.http.Query;

public interface GeoNamesServiceProxy
{

	@GET(value = "/timezoneJSON")
	TimeModifier getModifiers(@Query("lng") double longitude, @Query("lat") double lattitude, @Query("username") String username);
	
}
