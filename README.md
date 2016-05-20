# PopularMovies
App Feature implementations:
    Movies are displayed in the main layout via a grid of their corresponding movie poster thumbnails.
    UI contains settings menu to toggle the sort order of the movies by: most popular, highest rated.
    UI contains a screen for displaying the details for a selected movie.
    Movie Details layout contains title, release date, movie poster, vote average, and plot synopsis.
    Movie Details layout contains a section for displaying trailer videos and user reviews.
    Tablet UI uses a Master-Detail layout implemented using fragments. The left fragment is for discovering movies. The right fragment displays the movie details view for the currently selected movie.
User Interface - Functions
    When a user changes the sort criteria (most popular, highest rated, and favorites) the main view gets updated correctly.
    When a movie poster thumbnail is selected, the movie details screen is launched [Phone] or displayed in a fragment [Tablet].
    When a trailer is selected, app uses an Intent to launch the trailer.
    In the movies detail screen, a user can tap a button(a star) to mark it as a Favorite.
Network API Implementation
    In a background thread, app queries the /movie/popular or /movie/top_rated API for the sort criteria specified in the settings menu.
    App requests for related videos for a selected movie via the /movie/{id}/videos endpoint in a background thread and displays those details when the user selects a movie.
    App requests for user reviews for a selected movie via the /movie/{id}/reviews endpoint in a background thread and displays those details when the user selects a movie.


API Info:
  To fetch popular movies, you will use the API from themoviedb.org.
  If you don’t already have an account, you will need to create one in order to request an API Key.
  In your request for a key, state that your usage will be for educational/non-commercial use. You will also need to provide some personal information to complete the request. Once you submit your request, you should receive your key via email shortly after.
  In order to request popular movies you will want to request data from the /movie/popular and /movie/top_rated endpoints. An API Key is required.
  Once you obtain your key,  append it to HTTP request as a URL parameter like so:
  http://api.themoviedb.org/3/movie/popular?api_key=[YOUR_API_KEY]
  
