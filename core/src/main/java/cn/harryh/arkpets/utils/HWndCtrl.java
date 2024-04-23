/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinUser;

import java.util.ArrayList;


public class HWndCtrl {
    public final HWND hWnd;
    public final String windowText;
    public final Pointer windowPointer;
    public final int posTop;
    public final int posBottom;
    public final int posLeft;
    public final int posRight;
    public final int windowWidth;
    public final int windowHeight;

    public static final HWndCtrl EMPTY = new HWndCtrl();

    public static final int WS_EX_TOPMOST       = 0x00000008;
    public static final int WS_EX_TRANSPARENT   = 0x00000020;
    public static final int WS_EX_TOOLWINDOW    = 0x00000080;
    public static final int WS_EX_LAYERED       = 0x00080000;

    public static final int WM_MOUSEMOVE    = 0x0200;
    public static final int WM_LBUTTONDOWN  = 0x0201;
    public static final int WM_LBUTTONUP    = 0x0202;
    public static final int WM_RBUTTONDOWN  = 0x0204;
    public static final int WM_RBUTTONUP    = 0x0205;
    public static final int WM_MBUTTONDOWN  = 0x0207;
    public static final int WM_MBUTTONUP    = 0x0208;

    private static final int MK_LBUTTON  = 0x0001;
    private static final int MK_RBUTTON  = 0x0002;
    private static final int MK_MBUTTON  = 0x0010;


    /** HWnd Controller instance.
     * @param hWnd The handle of the window.
     */
    public HWndCtrl(HWND hWnd) {
        this.hWnd = hWnd;
        windowText = getWindowText(hWnd);
        windowPointer = getWindowIdx(hWnd);
        RECT rect = getWindowRect(hWnd);
        posTop = rect.top;
        posBottom = rect.bottom;
        posLeft = rect.left;
        posRight = rect.right;
        windowWidth = posRight-posLeft;
        windowHeight = posBottom-posTop;
    }

    /** HWnd Controller instance.
     * @param className The class name of the window.
     * @param windowName The title of the window.
     */
    public HWndCtrl(String className, String windowName) {
        this(User32.INSTANCE.FindWindow(className, windowName));
    }

    /** HWnd Controller instance.
     * @param pointer The pointer.
     */
    public HWndCtrl(int pointer) {
        this(new HWND(Pointer.createConstant(pointer)));
    }

    /** Empty HWnd Controller instance.
     */
    public HWndCtrl() {
        hWnd = null;
        windowText = null;
        windowPointer = null;
        posTop = 0;
        posBottom = 0;
        posLeft = 0;
        posRight = 0;
        windowWidth = 0;
        windowHeight = 0;
    }

    /** Returns true if the handle is empty.
     */
    public boolean isEmpty() {
        return hWnd == null;
    }

    /** Returns true if the window is a foreground window now.
     */
    public boolean isForeground() {
        if (isEmpty()) return false;
        return hWnd.equals(User32.INSTANCE.GetForegroundWindow());
    }

    /** Returns true if the window is visible now.
     */
    public boolean isVisible() {
        return isVisible(hWnd);
    }

    /** Gets the center X position.
     * @return X.
     */
    public float getCenterX() {
        return posLeft + windowWidth / 2f;
    }

    /** Gets the center Y position.
     * @return Y.
     */
    public float getCenterY() {
        return posTop + windowHeight / 2f;
    }

    /** Requests to close the window.
     * @param timeout Timeout for waiting response (ms).
     * @return true=success, false=failure.
     */
    public boolean close(int timeout) {
        return User32.INSTANCE.SendMessageTimeout(hWnd, 0x10, null, null, timeout, WinUser.SMTO_NORMAL, null).intValue() == 0;
    }

    /** Gets the value of the window's extended styles.
     * @return EX_STYLE value.
     * @see WinUser
     */
    public int getWindowExStyle() {
        if (isEmpty()) return 0;
        return User32.INSTANCE.GetWindowLong(hWnd, WinUser.GWL_EXSTYLE);
    }

    /** Sets the window as the foreground window.
     */
    public void setForeground() {
        User32.INSTANCE.SetForegroundWindow(hWnd);
    }

    /** Sets the window's transparency.
     * @param alpha Alpha value, from 0 to 1.
     */
    public void setWindowAlpha(float alpha) {
        if (isEmpty()) return;
        alpha = Math.max(0, Math.min(1, alpha));
        byte byteAlpha = (byte)((int)(alpha * 255) & 0xFF);
        User32.INSTANCE.SetLayeredWindowAttributes(hWnd, 0, byteAlpha, User32.LWA_ALPHA);
    }

    /** Sets the window's extended styles.
     * @param newLong New EX_STYLE value.
     * @see WinUser
     */
    public void setWindowExStyle(int newLong) {
        if (isEmpty()) return;
        User32.INSTANCE.SetWindowLong(hWnd, WinUser.GWL_EXSTYLE, newLong);
    }

    /** Sets the window's position without activating the window.
     * @param insertAfter The window to precede the positioned window in the Z order.
     * @param x The new position of the left side of the window, in client coordinates.
     * @param y The new position of the top of the window, in client coordinates.
     * @param w The new width of the window, in pixels.
     * @param h The new height of the window, in pixels.
     */
    public void setWindowPosition(HWndCtrl insertAfter, int x, int y, int w, int h) {
        if (isEmpty()) return;
        User32.INSTANCE.SetWindowPos(hWnd, insertAfter.hWnd, x, y, w, h, WinUser.SWP_NOACTIVATE);
    }

    /** Sets the window's ability to be passed through.
     * @param transparent Whether the window can be passed through.
     */
    public void setWindowTransparent(boolean transparent) {
        if (isEmpty()) return;
        if (transparent)
            setWindowExStyle(getWindowExStyle() | HWndCtrl.WS_EX_TRANSPARENT);
        else
            setWindowExStyle(getWindowExStyle() & ~HWndCtrl.WS_EX_TRANSPARENT);
    }

    /** Sends a mouse event message to the window.
     * @param msg The window message value.
     * @param x The X-axis coordinate, related to the left border of the window.
     * @param y The Y-axis coordinate, related to the top border of the window.
     */
    public void sendMouseEvent(int msg, int x, int y) {
        int wParam = switch (msg) {
            case WM_LBUTTONDOWN -> MK_LBUTTON;
            case WM_RBUTTONDOWN -> MK_RBUTTON;
            case WM_MBUTTONDOWN -> MK_MBUTTON;
            default -> 0;
        };
        int lParam = (y << 16) | x;
        User32.INSTANCE.SendMessage(hWnd, msg, new WinDef.WPARAM(wParam), new WinDef.LPARAM(lParam));
    }

    /** Gets a new HWndCtrl which contains the updated information of this window.
     * @return The up-to-dated HWndCtrl.
     */
    public HWndCtrl updated() {
        return new HWndCtrl(hWnd);
    }

    /** Gets the current list of windows.
     * @param only_visible Whether exclude the invisible window.
     * @return An ArrayList consists of HWndCtrls.
     */
    public static ArrayList<HWndCtrl> getWindowList(boolean only_visible) {
        ArrayList<HWndCtrl> windowList = new ArrayList<>();
        User32.INSTANCE.EnumWindows((hWnd, arg1) -> {
            if (User32.INSTANCE.IsWindow(hWnd) && (!only_visible || isVisible(hWnd)))
                windowList.add(new HWndCtrl(hWnd));
            return true;
        }, null);
        return windowList;
    }

    /** Gets the current list of windows. (Advanced)
     * @param only_visible Whether exclude the invisible window.
     * @param exclude_ws_ex Exclude the specific window-style-extra.
     * @return An ArrayList consists of HWndCtrls.
     */
    public static ArrayList<HWndCtrl> getWindowList(boolean only_visible, long exclude_ws_ex) {
        ArrayList<HWndCtrl> windowList = new ArrayList<>();
        User32.INSTANCE.EnumWindows((hWnd, arg1) -> {
            if (User32.INSTANCE.IsWindow(hWnd) && (!only_visible || isVisible(hWnd))
                    && (User32.INSTANCE.GetWindowLong(hWnd, WinUser.GWL_EXSTYLE) & exclude_ws_ex) != exclude_ws_ex)
                windowList.add(new HWndCtrl(hWnd));
            return true;
        }, null);
        return windowList;
    }

    private static boolean isVisible(HWND hWnd) {
        try {
            if (!User32.INSTANCE.IsWindowVisible(hWnd) || !User32.INSTANCE.IsWindowEnabled(hWnd))
                return false;
            RECT rect = getWindowRect(hWnd);
            if (rect.top == rect.bottom || rect.left == rect.right)
                return false;
        } catch(Exception e) {
            return false;
        }
        return true;
    }

    private static Pointer getWindowIdx(HWND hWnd) {
        return hWnd.getPointer();
    }

    private static String getWindowText(HWND hWnd) {
        char[] text = new char[1024];
        User32.INSTANCE.GetWindowText(hWnd, text, 512);
        return Native.toString(text);
    }

    private static  RECT getWindowRect(HWND hWnd) {
        RECT rect = new RECT();
        User32.INSTANCE.GetWindowRect(hWnd, rect);
        return rect;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HWndCtrl hWndCtrl = (HWndCtrl)o;
        return hWnd.equals(hWndCtrl.hWnd);
    }

    @Override
    public int hashCode() {
        return hWnd.hashCode();
    }

    @Override
    public String toString() {
        return "‘" + windowText + "’ " + windowWidth + "*" + windowHeight + " style=" + getWindowExStyle();
    }
}
