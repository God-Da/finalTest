import javazoom.jl.player.Player;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

// MusicPlayer 클래스: 음악 플레이어 GUI와 기능을 제공
public class MusicPlayer {
    // 재생목록 데이터를 저장하는 DefaultListModel (제네릭 컬렉션 사용)
    private final DefaultListModel<String> playlistModel = new DefaultListModel<>();
    private final JList<String> playlist = new JList<>(playlistModel); // GUI에서 재생목록 표시
    private Thread musicThread; // 음악 재생을 위한 별도의 스레드
    private volatile boolean isPlaying = false; // 음악 재생 상태를 저장
    private Player player; // JLPlayer 객체 (MP3 재생용)

    JLabel lbl, imgLbl; // 제목 라벨과 이미지 라벨
    JButton btnNext, btnPrevious, btnFirst, btnEnd, btnPause, btnPlay; // 버튼
    ImageIcon[] image, icon; // 이미지 및 아이콘 배열
    int flag = 0; // 현재 재생 중인 음악 및 이미지 인덱스

    // 음악 제목 데이터를 저장하는 맵 (제네릭 컬렉션 사용)
    private final Map<String, String> musicTitleMap = new HashMap<>();

    // 음악 플레이어 패널 생성
    public JPanel MusicPlayerPanel() {
        JPanel mainPanel = new JPanel(); // 메인 패널 생성
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS)); // 세로 정렬

        // 제목 라벨 설정
        lbl = new JLabel("♡ 연말에 들으면 좋은 음악 ♡", SwingConstants.CENTER);
        lbl.setFont(new Font("", Font.BOLD, 30));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT); // 중앙 정렬

        // 이미지 로드
        loadImages();

        // 이미지 라벨 초기화
        imgLbl = new JLabel(image[0], SwingConstants.CENTER);
        imgLbl.setPreferredSize(new Dimension(300, 300)); // 크기 설정
        imgLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 버튼 아이콘 로드
        loadIcons();

        // 버튼 생성 및 초기화
        btnFirst = new JButton(icon[0]);
        btnPrevious = new JButton(icon[1]);
        btnPlay = new JButton(icon[2]);
        btnPause = new JButton(icon[3]);
        btnNext = new JButton(icon[4]);
        btnEnd = new JButton(icon[5]);

        JButton[] buttons = {btnFirst, btnPrevious, btnPlay, btnPause, btnNext, btnEnd}; // 버튼 배열
        Dimension buttonSize = new Dimension(70, 100); // 버튼 크기 설정

        // 버튼 크기 및 스타일 설정
        for (JButton button : buttons) {
            button.setPreferredSize(buttonSize);
            button.setMargin(new Insets(0, 0, 0, 0));
            button.setContentAreaFilled(false);
        }

        // 버튼 이벤트 리스너 추가
        btnFirst.addActionListener(e -> selectMusic(0)); // 첫 곡 재생
        btnPrevious.addActionListener(e -> selectMusic((flag - 1 + playlistModel.size()) % playlistModel.size())); // 이전 곡
        btnNext.addActionListener(e -> selectMusic((flag + 1) % playlistModel.size())); // 다음 곡
        btnEnd.addActionListener(e -> selectMusic(playlistModel.size() - 1)); // 마지막 곡
        btnPlay.addActionListener(e -> playMusic()); // 재생
        btnPause.addActionListener(e -> stopMusic()); // 중지

        // 버튼 패널 생성
        JPanel buttonPanel = new JPanel(new FlowLayout());
        for (JButton button : buttons) {
            buttonPanel.add(button);
        }

        // 메인 패널에 컴포넌트 추가
        mainPanel.add(lbl);
        mainPanel.add(imgLbl);
        mainPanel.add(new JScrollPane(playlist)); // 음악 리스트 추가
        mainPanel.add(buttonPanel);

        // 음악 파일 로드
        loadMusicFiles();
        return mainPanel;
    }

    // 이미지 로드 메서드
    private void loadImages() {
        image = new ImageIcon[4]; // 이미지 배열 초기화
        for (int i = 0; i < image.length; i++) {
            String path = "image/p" + i + ".jpg";
            File file = new File(path);
            if (!file.exists()) {
                System.err.println("이미지 파일을 찾을 수 없습니다: " + path);
                image[i] = new ImageIcon(); // 빈 이미지로 대체
            } else {
                image[i] = new ImageIcon(path); // 이미지 로드
            }
        }
    }

    // 버튼 아이콘 로드 메서드
    private void loadIcons() {
        icon = new ImageIcon[6]; // 아이콘 배열 초기화
        for (int i = 0; i < icon.length; i++) {
            String path = "image/button" + i + ".png";
            File file = new File(path);
            if (!file.exists()) {
                System.err.println("아이콘 파일을 찾을 수 없습니다: " + path);
                icon[i] = new ImageIcon(); // 빈 아이콘으로 대체
            } else {
                Image img = new ImageIcon(path).getImage();
                Image resizedImage = img.getScaledInstance(50, 80, Image.SCALE_SMOOTH); // 크기 조정
                icon[i] = new ImageIcon(resizedImage);
            }
        }
    }

    // 음악 선택 메서드
    private void selectMusic(int index) {
        if (index >= 0 && index < playlistModel.size()) {
            playlist.setSelectedIndex(index); // 선택한 음악 업데이트
            flag = index; // 현재 재생 인덱스 업데이트
            if (index < image.length) {
                imgLbl.setIcon(image[index]); // 이미지 업데이트
            }
        }
    }

    // 음악 파일 로드 메서드
    private void loadMusicFiles() {
        // 음악 제목 데이터 로드
        loadMusicTitles();

        File musicDir = new File("music");
        if (!musicDir.exists() || !musicDir.isDirectory()) {
            JOptionPane.showMessageDialog(null, "music 폴더를 찾을 수 없습니다.");
            return;
        }

        // MP3 파일 필터링
        File[] files = musicDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                String title = musicTitleMap.getOrDefault(fileName, fileName); // 제목 로드
                playlistModel.addElement(title); // 재생목록에 추가
            }
        }

        // 기본 선택
        if (!playlistModel.isEmpty()) {
            playlist.setSelectedIndex(0);
        }
    }

    // 음악 제목 데이터 로드
    private void loadMusicTitles() {
        File titleFile = new File("music/musicTitles.txt");
        if (!titleFile.exists()) {
            System.err.println("musicTitles.txt 파일을 찾을 수 없습니다.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(titleFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    musicTitleMap.put(parts[0].trim(), parts[1].trim()); // 파일명과 제목 매핑
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 음악 재생 메서드
    private void playMusic() {
        if (musicThread != null && musicThread.isAlive()) {
            JOptionPane.showMessageDialog(null, "이미 음악이 재생 중입니다.");
            return;
        }

        String selectedMusic = playlist.getSelectedValue(); // 선택된 음악 제목
        if (selectedMusic == null) {
            JOptionPane.showMessageDialog(null, "재생할 음악을 선택하세요.");
            return;
        }

        String fileName = getFileNameByTitle(selectedMusic); // 제목을 파일명으로 변환

        musicThread = new Thread(() -> {
            try {
                File musicFile = new File("music/" + fileName);
                FileInputStream fis = new FileInputStream(musicFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                player = new Player(bis); // 음악 플레이어 초기화

                isPlaying = true;
                player.play(); // 음악 재생
                isPlaying = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        musicThread.start();
    }

    // 제목에서 파일명 찾기
    private String getFileNameByTitle(String title) {
        for (Map.Entry<String, String> entry : musicTitleMap.entrySet()) { // 제네릭 Map 사용
            if (entry.getValue().equals(title)) {
                return entry.getKey();
            }
        }
        return title; // 제목이 없으면 기본 파일명 반환
    }

    // 음악 중지 메서드
    private void stopMusic() {
        try {
            if (player != null) {
                player.close(); // 음악 중지
            }

            if (musicThread != null && musicThread.isAlive()) {
                musicThread.interrupt(); // 스레드 중단
            }

            isPlaying = false;
            JOptionPane.showMessageDialog(null, "음악 재생이 중단되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
