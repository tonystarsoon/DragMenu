package it.com.dragmenu.remote;

import it.com.dragmenu.model.SOAnswersResponse;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface RxService {

    @GET("/answers?order=desc&sort=activity&site=stackoverflow")
    Observable<SOAnswersResponse> getAnswers();

    @GET("/answers?order=desc&sort=activity&site=stackoverflow")
    Observable<SOAnswersResponse> getAnswers(@Query("tagged") String tags);
}