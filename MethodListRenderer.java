import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MethodListRenderer extends JPanel implements ListCellRenderer<MethodCheckBox> {
    private final JCheckBox checkBox = new JCheckBox();
    private final JLabel markerLabel = new JLabel();

    public MethodListRenderer() {
        setLayout(new BorderLayout());
        checkBox.setOpaque(false);
        markerLabel.setBorder(new EmptyBorder(0, 5, 0, 0));
        add(checkBox, BorderLayout.CENTER);
        add(markerLabel, BorderLayout.EAST);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends MethodCheckBox> list,
                                                  MethodCheckBox value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        checkBox.setText(value.methodInfo.name);
        checkBox.setSelected(value.isSelected());
        checkBox.setFont(value.getFont());

        if (value.methodInfo.hasMarker) {
            markerLabel.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
            markerLabel.setToolTipText("Сообщение уже добавлено");
        } else {
            markerLabel.setIcon(null);
            markerLabel.setToolTipText(null);
        }

        setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
        setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
        setBorder(new EmptyBorder(2, 2, 2, 2));
        return this;
    }
}