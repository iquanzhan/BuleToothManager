package com.chengxiaoxiao.buletoothmanager;

import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Intent;
import android.os.Parcelable;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

@SuppressLint("NewApi")
public class RobServices extends AccessibilityService {

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		// 可以接收发来的事件
		int eventType = event.getEventType();

		String sourcePackageName = (String) event.getPackageName();
		sourcePackageName = sourcePackageName.trim();

		switch (eventType) {
		case AccessibilityEvent.TYPE_VIEW_CLICKED:
			// Toast.makeText(this, "点击了", 0).show();
			break;

		case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
			// Toast.makeText(this, "文字改变了", 0).show();
			// inputClick("com.chengxiaoxiao.didimoni:id/btnGet");
			break;
		case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
			// 获取事件具体信息
			Parcelable parcelable = event.getParcelableData();
			// 如果是下拉通知栏消息
			if (parcelable instanceof Notification) {

			} else {

				// Toast.makeText(this, "包名："+sourcePackageName+",事件ID" +
				// eventType, 0).show();
				// 其它通知信息，包括Toast
				String toastMsg = (String) event.getText().get(0);

				toastMsg = toastMsg.trim();

				if (toastMsg.equals("滴滴抢单中")) {
					// Toast.makeText(this, "包名："+sourcePackageName+",事件ID" +
					// eventType, 0).show();

					// CharSequence name = event.getClassName();

					inputClick("com.chengxiaoxiao.didimoni:id/btnGet",
							getRootInActiveWindow());
				}

			}
			break;

		}

	}

	/**
	 * 根据id,获取AccessibilityNodeInfo，并点击。
	 */
	private void inputClick(String clickId, AccessibilityNodeInfo nodeInfo) {

		if (nodeInfo != null) {
			List<AccessibilityNodeInfo> list = nodeInfo
					.findAccessibilityNodeInfosByViewId(clickId);
			for (AccessibilityNodeInfo item : list) {
				item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
			}
		}
	}

	@Override
	public void onInterrupt() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onServiceConnected() {
		// 成功连接上服务时调用，可以做初始化工作

		Toast.makeText(this, "连接服务成功,你可以放心使用啦", 0).show();

	}

	@Override
	public boolean onUnbind(Intent intent) {
		// 服务被关闭时调用，可以做释放操作
		return super.onUnbind(intent);
	}

}
