package org.cocos2dx.javascript;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
/**
 * @author liyihe
 */
public class HookService extends AccessibilityService {
    public HookService() {
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        HookReceiver.preferGlobalHome(500);
    }

    @Override
    public void onInterrupt() {

    }
}