package com.sam_chordas.android.stockhawk.ui.mvp.presenters;

import de.greenrobot.event.EventBus;

/**
 * Created by aung on 3/27/16.
 */
public abstract class BasePresenter {

    public void onCreate(){
        EventBus eventBus = EventBus.getDefault();
        if (!eventBus.isRegistered(this)) {
            eventBus.register(this);
        }
    }

    public abstract void onStart();

    public abstract void onStop();

    public void onDestroy(){
        EventBus eventBus = EventBus.getDefault();
        eventBus.unregister(this);
    }
}
