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
		// ���Խ��շ������¼�
		int eventType = event.getEventType();

		String sourcePackageName = (String) event.getPackageName();
		sourcePackageName = sourcePackageName.trim();

		switch (eventType) {
		case AccessibilityEvent.TYPE_VIEW_CLICKED:
			// Toast.makeText(this, "�����", 0).show();
			break;

		case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
			// Toast.makeText(this, "���ָı���", 0).show();
			// inputClick("com.chengxiaoxiao.didimoni:id/btnGet");
			break;
		case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
			// ��ȡ�¼�������Ϣ
			Parcelable parcelable = event.getParcelableData();
			// ���������֪ͨ����Ϣ
			if (parcelable instanceof Notification) {

			} else {

				// Toast.makeText(this, "������"+sourcePackageName+",�¼�ID" +
				// eventType, 0).show();
				// ����֪ͨ��Ϣ������Toast
				String toastMsg = (String) event.getText().get(0);

				toastMsg = toastMsg.trim();

				if (toastMsg.equals("�ε�������")) {
					// Toast.makeText(this, "������"+sourcePackageName+",�¼�ID" +
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
	 * ����id,��ȡAccessibilityNodeInfo���������
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
		// �ɹ������Ϸ���ʱ���ã���������ʼ������

		Toast.makeText(this, "���ӷ���ɹ�,����Է���ʹ����", 0).show();

	}

	@Override
	public boolean onUnbind(Intent intent) {
		// ���񱻹ر�ʱ���ã��������ͷŲ���
		return super.onUnbind(intent);
	}

}
