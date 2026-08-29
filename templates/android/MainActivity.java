package ::APP_PACKAGE::;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

public class MainActivity extends org.haxe.lime.GameActivity {

	/**
	 * 强制进入并锁定真·沉浸模式。
	 *
	 * 需要同时做到三件事，缺一不可，否则「状态栏 / 底部手势小白条」在某些设备上
	 * 或从后台切回后仍会弹出挡画面：
	 *   1. edge-to-edge：让内容延伸到系统栏区域（decorFitsSystemWindows = false）。
	 *      尤其是在 Android 15（API 35）上，官方强制 edge-to-edge，仅用旧的
	 *      SYSTEM_UI_FLAG 已无法隐藏手势导航条。
	 *   2. 隐藏 systemBars（状态栏 + 导航栏）。
	 *   3. 行为设为「下滑临时唤起后自动再隐藏」（sticky / transient-by-swipe），
	 *      并在 onWindowFocusChanged 重新获得焦点时再次隐藏，避免切回来又冒出来。
	 */
	private void applyImmersive() {
		Window window = getWindow();
		View decor = (window == null) ? null : window.getDecorView();
		if (decor == null) return;

		// 屏幕常亮（游戏场景很有用）。
		decor.setKeepScreenOn(true);

		// 允许内容延伸到切角区域（挖孔屏 / 刘海屏）。
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			WindowManager.LayoutParams lp = window.getAttributes();
			lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
			window.setAttributes(lp);
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			// ---- Android 11+ 推荐的 WindowInsetsController 方案 ----
			// edge-to-edge：内容布满全屏（API 30+ 原生支持）。
			window.setDecorFitsSystemWindows(false);
			WindowInsetsController controller = decor.getWindowInsetsController();
			if (controller != null) {
				// 隐藏状态栏与导航栏（含手势小白条）。
				controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
				// 向下滑可临时唤出系统栏，随后自动再次隐藏（sticky 效果）。
				controller.setSystemBarsBehavior(
					WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
			}
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// ---- 旧版本（API 19~29）使用传统 flag ----
			// 透明状态栏背景，并让内容延伸过去（edge-to-edge 等效）。
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				window.setStatusBarColor(Color.TRANSPARENT);
			}
			decor.setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 状态栏 / 导航栏背景与系统栏图标也一并管理，避免出现「小白条」。
		Window window = getWindow();
		if (window != null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				window.setStatusBarColor(Color.TRANSPARENT);
				window.setNavigationBarColor(Color.TRANSPARENT);
			}
			window.getDecorView().setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
		}

		applyImmersive();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		// 关键：每次重新获得焦点（从弹窗、桌面、最近任务切回，或熄屏亮屏）都重进沉浸模式，
		// 否则系统栏会重新弹出并遮挡画面。
		if (hasFocus) {
			applyImmersive();
		}
	}
}