package com.forthorn.projecting.baserx;


import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * des:对服务器返回数据成功和失败处理
 * Created by xsf
 * on 2016.09.9:59
 */

/**************
 * 使用例子
 ******************/
/*_apiService.login(mobile, verifyCode)
        .compose(RxSchedulersHelper.io_main())
        .compose(RxResultHelper.handleResult())
        .//省略*/

public class RxHelper {
    /**
     * 对服务器返回数据进行预处理
     *
     * @param <T>
     * @return
     */
    public static <T> Observable.Transformer<BaseResponse<T>, T> handleResult() {
        return new Observable.Transformer<BaseResponse<T>, T>() {
            @Override
            public Observable<T> call(Observable<BaseResponse<T>> tObservable) {
                return tObservable.flatMap(new Func1<BaseResponse<T>, Observable<T>>() {
                    @Override
                    public Observable<T> call(BaseResponse<T> result) {
                        if (result.success()) {
                            return createData(result.data);
                        } else {
                            if (result.code.equals("450")) {
                                return Observable.error(new ServerException(result.code));
                            } else {
                                return Observable.error(new ServerException(result.msg));
                            }
                        }
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    public static Observable.Transformer<BaseResponse, String> handleMsgResult() {
        return new Observable.Transformer<BaseResponse, String>() {
            @Override
            public Observable<String> call(Observable<BaseResponse> tObservable) {
                return tObservable.flatMap(new Func1<BaseResponse, Observable<String>>() {
                    @Override
                    public Observable<String> call(BaseResponse result) {
                        if (result.success()) {
                            return createMsgData(result.msg);
                        } else {
                            return Observable.error(new ServerException(result.msg));
                        }
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    private static Observable<String> createMsgData(final String msg) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    subscriber.onNext(msg);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }

        });
    }


    /**
     * 创建成功的数据
     *
     * @param data
     * @param <T>
     * @return
     */
    private static <T> Observable<T> createData(final T data) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                try {
                    subscriber.onNext(data);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }
}
