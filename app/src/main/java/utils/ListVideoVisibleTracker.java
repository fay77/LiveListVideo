package utils;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.TimeUnit;

import listvideo.ListVideoPlayer;
import listvideo.ListVideoPlayerManager;
import models.RectView;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import scrollablelayout.ScrollableLayout;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

public class ListVideoVisibleTracker {

    private static Subscription visibleVideoViewSubscription;

    private static WeakReference<ViewGroup> screenViewWeakReference;

    private static Rect parentRect;

    private static RecyclerView.OnScrollListener recyclerScrollListener = new RecyclerView
            .OnScrollListener() {
        int currentState = SCROLL_STATE_IDLE;

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            CommonUtil.unSubscribeSubs(visibleVideoViewSubscription);
            currentState = newState;
            if (newState == SCROLL_STATE_IDLE) {
                findVisibleVideoView();
            }
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            CommonUtil.unSubscribeSubs(visibleVideoViewSubscription);
            if (currentState == SCROLL_STATE_IDLE) {
                findVisibleVideoView();
            } else {
                ListVideoPlayerManager.onPlayPause();
            }
        }
    };


    private static ScrollableLayout.OnScrollListener scrollableScrollListener = new
            ScrollableLayout.OnScrollListener() {


        int lastY = -1;

        @Override
        public void onScroll(int currentY, int maxY) {
            if (lastY != currentY) {
                lastY = currentY;
                ListVideoPlayerManager.onPlayPause();
                CommonUtil.unSubscribeSubs(visibleVideoViewSubscription);
                findVisibleVideoView();
            }
        }
    };


    private static ViewGroup getScreenView() {
        return screenViewWeakReference != null ? screenViewWeakReference.get() : null;
    }

    public static void setScreenView(ViewGroup screenView) {
        if (screenView == null) {
            return;
        }
        View lastView = getScreenView();
        if (lastView != null && lastView == screenView) {
            findVisibleVideoView();
            return;
        } else if (lastView != null) {
            CommonUtil.unSubscribeSubs(visibleVideoViewSubscription);
            ListVideoPlayerManager.releaseAllVideos();
            screenViewWeakReference.clear();
        }
        screenViewWeakReference = new WeakReference<>(screenView);
        findVisibleVideoView();
    }


    public static void removeScreenView(ViewGroup pageView) {
        View lastView = getScreenView();
        if (lastView != null && lastView == pageView) {
            CommonUtil.unSubscribeSubs(visibleVideoViewSubscription);
            ListVideoPlayerManager.releaseAllVideos();
            screenViewWeakReference.clear();
        }
    }


    public static void addScrollView(ViewGroup... views) {
        for (ViewGroup view : views) {
            View scrollView = view;
//            if (view instanceof PullToRefreshBase) {
//                scrollView = ((PullToRefreshBase) view).getRefreshableView();
//            }
            if (scrollView instanceof RecyclerView) {
                ((RecyclerView) scrollView).addOnScrollListener(recyclerScrollListener);
            } else if (scrollView instanceof ScrollableLayout) {
                ((ScrollableLayout) scrollView).addOnScrollListener(scrollableScrollListener);
            }
        }
        findVisibleVideoView();
    }

    public static void removeScrollView(ViewGroup... views) {
        for (ViewGroup view : views) {
            View scrollView = view;
//            if (view instanceof PullToRefreshBase) {
//                scrollView = ((PullToRefreshBase) view).getRefreshableView();
//            }
            if (scrollView instanceof RecyclerView) {
                ((RecyclerView) scrollView).removeOnScrollListener(recyclerScrollListener);
            } else if (scrollView instanceof ScrollableLayout) {
                ((ScrollableLayout) scrollView).removeOnScrollListener(scrollableScrollListener);
            }
        }
        findVisibleVideoView();
    }


    private static void findVisibleVideoView() {
        CommonUtil.unSubscribeSubs(visibleVideoViewSubscription);
        visibleVideoViewSubscription = Observable.timer(500, TimeUnit.MILLISECONDS)
                .concatMap(new Func1<Long, Observable<RectView>>() {
                    @Override
                    public Observable<RectView> call(Long aLong) {
                        return Observable.create(new Observable.OnSubscribe<RectView>() {
                            @Override
                            public void call(Subscriber<? super RectView> subscriber) {
                                ViewGroup screenView = getScreenView();
                                if (screenView != null && screenView.isShown()) {
                                    parentRect = new Rect();
                                    screenView.getGlobalVisibleRect(parentRect);
                                    Rect windowRect = new Rect();
                                    screenView.getWindowVisibleDisplayFrame(windowRect);
                                    if (Rect.intersects(parentRect, windowRect)) {
                                        subscriber.onNext(new RectView(screenView, parentRect));
                                        subscriber.onCompleted();
                                        return;
                                    }else {
                                        Log.e("ListVideoVisibleTracker","parentRect:"+parentRect.toString()+"\n"+"windowRect:"+windowRect.toString());
                                    }
                                }
                                subscriber.onError(new Exception("视频列表不在当前屏幕中"));
                            }
                        });
                    }
                })
                .concatMap(new Func1<RectView, Observable<RectView>>() {
                    @Override
                    public Observable<RectView> call(RectView screenRectView) {
                        return findVideoObb(screenRectView);
                    }
                })
                .toSortedList(new Func2<RectView, RectView, Integer>() {
                    @Override
                    public Integer call(RectView rectView, RectView rectView2) {
                        return rectView.rect.top - rectView2.rect.top;
                    }
                })
                .onErrorReturn(new Func1<Throwable, List<RectView>>() {
                    @Override
                    public List<RectView> call(Throwable throwable) {
                        throwable.printStackTrace();
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<RectView>>() {
                    @Override
                    public void call(List<RectView> rectViews) {
                        if (CommonUtil.isCollectionEmpty(rectViews) || getScreenView() == null ||
                                !VideoUtil.isWifiConnected(
                                getScreenView().getContext())) {
                            ListVideoPlayerManager.releaseAllVideos();
                        } else {
                            RectView nestRectView = null;
                            boolean onPause = false;
                            for (RectView rectView : rectViews) {
                                onPause |= ((ListVideoPlayer) rectView.view).isCurrentVideo();
                                if (rectView.rect.height() != rectView.view.getHeight()) {
                                    continue;
                                }
                                nestRectView=rectView;
                                break;
                            }
                            if (nestRectView != null) {
                                playVideoView((ListVideoPlayer) nestRectView.view);
                            } else if (onPause) {
                                ListVideoPlayerManager.onPlayPause();
                            } else {
                                ListVideoPlayerManager.releaseAllVideos();
                            }
                        }

                    }
                });
    }

    private static void playVideoView(ListVideoPlayer player) {
        if (player.isCurrentVideo()) {
            ListVideoPlayerManager.onPlayPlaying();
        } else {
            player.startVideo();
        }
    }


    private static Observable<RectView> findVideoObb(final RectView parentRectView) {
        return Observable.create(new Observable.OnSubscribe<RectView>() {
            @Override
            public void call(Subscriber<? super RectView> subscriber) {
                if (parentRectView.view instanceof ViewGroup) {
                    ViewGroup parentView = (ViewGroup) parentRectView.view;
                    for (int i = 0, size = parentView.getChildCount(); i < size; i++) {
                        View childView = parentView.getChildAt(i);
                        if (childView!=null&&childView.isShown()) {
                            Rect rect = new Rect();
                            childView.getGlobalVisibleRect(rect);
                            if (Rect.intersects(rect, parentRect)) {
                                subscriber.onNext(new RectView(childView, rect));
                            }
                        }
                    }
                }
                subscriber.onCompleted();
            }
        })
                .concatMap(new Func1<RectView, Observable<? extends RectView>>() {
                    @Override
                    public Observable<? extends RectView> call(RectView rectView) {
                        if (!(rectView.view instanceof ListVideoPlayer) && rectView.view
                                instanceof ViewGroup) {
                            return findVideoObb(rectView);
                        }
                        return Observable.just(rectView);
                    }
                })
                .filter(new Func1<RectView, Boolean>() {
                    @Override
                    public Boolean call(RectView rectView) {
                        return rectView.view instanceof ListVideoPlayer;
                    }
                });
    }
}
