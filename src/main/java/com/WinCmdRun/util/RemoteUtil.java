import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JOptionPane;

/**
 * 远程链接工具类
 */
public class RemoteUtil {

    /**
     * 打开远程链接（通过系统默认浏览器）
     * @param remoteUrl 远程链接URL
     */
    public static void openRemoteLink(String remoteUrl) {
        // 检查URL是否为空
        if (remoteUrl == null || remoteUrl.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "远程链接为空",
                    "提示",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String url = remoteUrl.trim();
        // 简单处理URL格式，确保以http/https开头
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }

        try {
            // 检查系统是否支持Desktop功能
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                // 检查是否支持浏览操作
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    // 打开默认浏览器访问URL
                    desktop.browse(new URI(url));
                } else {
                    JOptionPane.showMessageDialog(null,
                            "系统不支持浏览器操作",
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null,
                        "系统不支持桌面操作",
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (URISyntaxException e) {
            JOptionPane.showMessageDialog(null,
                    "链接格式不正确: " + url,
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "无法打开链接: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}