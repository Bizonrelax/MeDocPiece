import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MethodCheckBox extends JCheckBox {
    public final MethodInfo methodInfo;
    private final JLabel markerLabel;

    public MethodCheckBox(MethodInfo info) {
        super(info.name);
        this.methodInfo = info;
        
        setLayout(new BorderLayout());
        // Иконка будет справа от текста
        setHorizontalTextPosition(SwingConstants.LEADING);
        
        markerLabel = new JLabel();
        markerLabel.setBorder(new EmptyBorder(0, 5, 0, 0));
        add(markerLabel, BorderLayout.EAST);
        
        updateMarkerStatus(info.hasMarker);
    }

    public void updateMarkerStatus(boolean hasMarker) {
        this.methodInfo.hasMarker = hasMarker;
        if (hasMarker) {
            // Используем стандартную иконку информации
            markerLabel.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
            markerLabel.setToolTipText("Сообщение уже добавлено");
        } else {
            markerLabel.setIcon(null);
            markerLabel.setToolTipText(null);
        }
    }
    
    public void setFontSize(int newSize) {
        setFont(getFont().deriveFont((float) newSize));
        // Обновляем и размер шрифта в тултипе
        markerLabel.setFont(markerLabel.getFont().deriveFont((float) newSize));
    }
}