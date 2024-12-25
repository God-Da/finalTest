import javax.swing.*;
import java.awt.*;

// 메인 클래스 - JFrame을 상속받아 GUI 애플리케이션을 구현
public class TaskMusicApp extends JFrame {
    // 음악 플레이어 객체 생성
    private final MusicPlayer musicPlayer = new MusicPlayer();
    // 할 일 관리 객체 생성
    private final TaskManager taskManager = new TaskManager();

    // 생성자 - GUI 초기 설정 및 구성
    public TaskMusicApp() {
        // 프레임 제목 설정
        setTitle("다연씨를 위한...할 일 관리 겸 음악 플레이어");
        // 프레임 크기 설정
        setSize(1200, 600);
        // 창 닫기 버튼 클릭 시 애플리케이션 종료
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // 레이아웃을 BorderLayout으로 설정
        setLayout(new BorderLayout());

        // 음악 플레이어 패널 생성 (왼쪽)
        JPanel musicPanel = musicPlayer.MusicPlayerPanel();

        // 할 일 관리 패널 생성 (오른쪽)
        JPanel taskPanel = taskManager.TaskManagerPanel();

        // JSplitPane을 사용하여 두 패널을 수평으로 나누기
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, musicPanel, taskPanel);
        // 초기 분할 위치를 전체 너비의 60%로 설정 (6:4 비율)
        splitPane.setDividerLocation(0.6);
        // 크기 조정 시 양쪽 패널 비율 유지 (6:4)
        splitPane.setResizeWeight(0.6);
        // JSplitPane을 프레임 중앙에 추가
        add(splitPane, BorderLayout.CENTER);

        // 프레임 표시
        setVisible(true);
    }

    // 메인 메서드 - 프로그램 실행 진입점
    public static void main(String[] args) {
        // Swing 이벤트 디스패치 스레드에서 TaskMusicApp 실행
        SwingUtilities.invokeLater(TaskMusicApp::new);
    }
}
