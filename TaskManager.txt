import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

// 할 일 관리 클래스
public class TaskManager {
    // 제네릭 컬렉션 List를 사용하여 할 일 목록 관리
    private final List<String> taskList = new ArrayList<>();

    // JTable의 데이터 모델 설정 (테이블에 "할 일"과 "상태" 열 생성)
    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"할 일", "상태"}, 0) {
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            // 상태 열을 Boolean 타입으로 설정하여 체크박스로 표시
            return columnIndex == 1 ? Boolean.class : String.class;
        }
    };

    // 메인 패널 생성 및 구성 메서드
    public JPanel TaskManagerPanel() {
        JPanel panel = new JPanel(); // 메인 패널 생성
        panel.setLayout(new BorderLayout()); // BorderLayout으로 레이아웃 설정

        // 할 일 테이블 생성 및 설정
        JTable taskTable = new JTable(tableModel);
        taskTable.setRowHeight(25); // 행 높이 조정

        // 테이블 정렬을 위한 TableRowSorter 추가
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        taskTable.setRowSorter(sorter);

        // 테이블에 스크롤 기능 추가
        JScrollPane scrollPane = new JScrollPane(taskTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 버튼 생성
        JButton addTaskButton = new JButton("추가");
        JButton removeTaskButton = new JButton("삭제");
        JButton saveTasksButton = new JButton("저장");
        JButton loadTasksButton = new JButton("불러오기");
        JButton removeCompletedTasksButton = new JButton("완료 항목 삭제");

        // 버튼 클릭 이벤트 처리
        addTaskButton.addActionListener(e -> addTask());
        removeTaskButton.addActionListener(e -> removeTask(taskTable));
        saveTasksButton.addActionListener(e -> saveTasks());
        loadTasksButton.addActionListener(e -> loadTasks());
        removeCompletedTasksButton.addActionListener(e -> removeCompletedTasks());

        // 버튼을 포함한 하단 패널 추가
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addTaskButton);
        buttonPanel.add(removeTaskButton);
        buttonPanel.add(saveTasksButton);
        buttonPanel.add(loadTasksButton);
        buttonPanel.add(removeCompletedTasksButton); // 완료 항목 삭제 버튼 추가
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel; // 완성된 패널 반환
    }

    // 할 일 추가 메서드
    private void addTask() {
        // 사용자에게 할 일을 입력받음
        String task = JOptionPane.showInputDialog("할 일을 입력하세요:");
        if (task != null && !task.isBlank()) { // 입력값이 null이거나 공백이 아닌 경우 처리
            taskList.add(task); // List에 새로운 할 일 추가 (제네릭 컬렉션 사용)
            tableModel.addRow(new Object[]{task, false}); // 테이블에 미완료 상태로 추가
        }
    }

    // 선택한 할 일 삭제 메서드
    private void removeTask(JTable taskTable) {
        int selectedRow = taskTable.getSelectedRow(); // 선택된 행의 인덱스 가져오기
        if (selectedRow >= 0) {
            // 정렬된 상태에서도 올바른 행 삭제를 위해 모델 인덱스로 변환
            int modelRow = taskTable.convertRowIndexToModel(selectedRow);
            taskList.remove(modelRow); // List에서 해당 항목 제거
            tableModel.removeRow(modelRow); // 테이블에서 해당 행 삭제
        } else {
            JOptionPane.showMessageDialog(null, "삭제할 항목을 선택하세요."); // 선택되지 않은 경우 경고 메시지
        }
    }

    // 완료된 할 일 삭제 메서드
    private void removeCompletedTasks() {
        // 뒤에서부터 삭제하여 인덱스 문제가 발생하지 않도록 처리
        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            boolean status = (Boolean) tableModel.getValueAt(i, 1); // 상태 값 가져오기
            if (status) { // 상태가 true(완료)인 경우
                taskList.remove(i); // List에서 해당 항목 제거
                tableModel.removeRow(i); // 테이블에서 해당 행 삭제
            }
        }
        JOptionPane.showMessageDialog(null, "완료된 항목이 모두 삭제되었습니다."); // 완료 메시지
    }

    // 할 일 저장 메서드
    private void saveTasks() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("tasks.txt"))) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String task = (String) tableModel.getValueAt(i, 0); // 할 일 가져오기
                boolean status = (Boolean) tableModel.getValueAt(i, 1); // 상태 가져오기
                writer.write(task + "," + status); // 할 일과 상태를 CSV 형식으로 저장, 파일이 없으면 새로 생성되고 있으면 덮어쓰기 된다.
                writer.newLine(); // 줄바꿈
            }
            JOptionPane.showMessageDialog(null, "할 일이 저장되었습니다."); // 저장 완료 메시지
        } catch (IOException e) {
            e.printStackTrace(); // 예외 발생 시 스택 트레이스 출력
        }
    }

    // 할 일 불러오기 메서드
    private void loadTasks() {
        try (BufferedReader reader = new BufferedReader(new FileReader("tasks.txt"))) {
            taskList.clear(); // 기존 할 일 목록 초기화
            tableModel.setRowCount(0); // 테이블 초기화
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 2); // CSV 형식에서 할 일과 상태 분리
                String task = parts[0]; // 할 일
                boolean status = Boolean.parseBoolean(parts[1]); // 상태를 Boolean으로 변환
                taskList.add(task); // List에 추가 (제네릭 컬렉션 사용)
                tableModel.addRow(new Object[]{task, status}); // 테이블에 추가
            }
            JOptionPane.showMessageDialog(null, "할 일이 불러와졌습니다."); // 불러오기 완료 메시지
        } catch (IOException e) {
            e.printStackTrace(); // 예외 발생 시 스택 트레이스 출력
        }
    }
}
