/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.weyoung.player.ui;


import org.weyoung.player.R;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

/**
 * The playback controller for the Movie Player.
 */
public class MovieControllerOverlay extends CommonControllerOverlay implements
        AnimationListener {

    private boolean hidden;

    private final Handler handler;
    private final Runnable startHidingRunnable;
    private final Animation hideAnimation;

    int mOldX, mOldY;
    private static final float HSLOPE_TO_START_SNAP = .25f;
    private static final float VSLOPE_TO_START_SNAP = 1.0f;
    private static final int MOVE_NONE = 0x0;
    private static final int MOVE_VERTICAL = 0x1;
    private static final int MOVE_HORIZION = 0x2;
    private static int mMoveMode = MOVE_NONE;

    public MovieControllerOverlay(Context context) {
        super(context);

        handler = new Handler();
        startHidingRunnable = new Runnable() {
                @Override
            public void run() {
                startHiding();
            }
        };

        hideAnimation = AnimationUtils.loadAnimation(context, R.anim.player_out);
        hideAnimation.setAnimationListener(this);

        hide();
    }

    @Override
    protected void createTimeBar(Context context) {
        mTimeBar = new TimeBar(context, this);
    }

    @Override
    public void hide() {
        boolean wasHidden = hidden;
        hidden = true;
        super.hide();
        if (mListener != null && wasHidden != hidden) {
            mListener.onHidden();
        }
    }


    @Override
    public void show() {
        boolean wasHidden = hidden;
        hidden = false;
        super.show();
        if (mListener != null && wasHidden != hidden) {
            mListener.onShown();
        }
        maybeStartHiding();
    }

    private void maybeStartHiding() {
        cancelHiding();
        if (mState == State.PLAYING) {
            handler.postDelayed(startHidingRunnable, 2500);
        }
    }

    private void startHiding() {
        startHideAnimation(mBackground);
        startHideAnimation(mTimeBar);
        startHideAnimation(mPlayPauseReplayView);
    }

    private void startHideAnimation(View view) {
        if (view.getVisibility() == View.VISIBLE) {
            view.startAnimation(hideAnimation);
        }
    }

    private void cancelHiding() {
        handler.removeCallbacks(startHidingRunnable);
        mBackground.setAnimation(null);
        mTimeBar.setAnimation(null);
        mPlayPauseReplayView.setAnimation(null);
    }

    @Override
    public void onAnimationStart(Animation animation) {
        // Do nothing.
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        // Do nothing.
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        hide();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (hidden) {
            show();
        }
        return super.onKeyDown(keyCode, event);
    }


    private float calculateDragAngle(int dx, int dy) {
        dx = Math.abs(dx);
        dy = Math.abs(dy);
        return (float) Math.atan2(dy, dx);
    }
    //SOGOU TODO
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (super.onTouchEvent(event)) {
            return true;
        }
        if (hidden) {
            show();
            return true;
        }
        int deltaX = mOldX - Math.round(event.getX());
        int deltaY = mOldY - Math.round(event.getY());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                cancelHiding();
                mOldX = Math.round(event.getX());
                mOldY = Math.round(event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                float angle = calculateDragAngle(deltaX, deltaY);
                int distance = (int)Math.sqrt(deltaX*deltaX + deltaY*deltaY);
                if(angle < HSLOPE_TO_START_SNAP && (mMoveMode == MOVE_HORIZION 
                        || mMoveMode == MOVE_NONE)) {
                    mMoveMode = MOVE_HORIZION;
                    int direction = deltaX > 0 ? -1 : 1;
                    mTimeBar.seekTimeBar(direction * distance);
                }else if(angle > VSLOPE_TO_START_SNAP && (mMoveMode == MOVE_VERTICAL 
                        || mMoveMode == MOVE_NONE)) {
                    mMoveMode = MOVE_VERTICAL;
                    int direction = deltaY < 0 ? -1 : 1;
                    float percent = (float)distance/getHeight() * direction;
                    if(mOldX < getWidth()/2)
                        mListener.onVolumeChange(percent);
                    else
                        mListener.onBrightnessChange(percent);
                }
                mOldX = Math.round(event.getX());
                mOldY = Math.round(event.getY());
                break;
            case MotionEvent.ACTION_UP:
                maybeStartHiding();
                mMoveMode = MOVE_NONE;
                break;
        }
        return true;
    }

    @Override
    protected void updateViews() {
        if (hidden) {
            return;
        }
        super.updateViews();
    }

    // TimeBar listener

    @Override
    public void onScrubbingStart() {
        cancelHiding();
        super.onScrubbingStart();
    }

    @Override
    public void onScrubbingMove(int time) {
        cancelHiding();
        super.onScrubbingMove(time);
    }

    @Override
    public void onScrubbingEnd(int time, int trimStartTime, int trimEndTime) {
        maybeStartHiding();
        super.onScrubbingEnd(time, trimStartTime, trimEndTime);
    }
}

