import java.io.*;
import java.util.*;
public class Main {
    static int[] dy = {0, 0, 1, -1}; // 동 서 남 북
    static int[] dx = {1, -1, 0, 0};
    static int[][] floorMap, vMap;
    static int[][][] timeMap;
    static List<int[]> anomaly;
    static int N, M, F, ans;
    static int machineX, machineY, machineLoc, wallEndX, wallEndY, endX, endY, entranceDir, entranceX, entranceY;
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        StringTokenizer st = new StringTokenizer(br.readLine());
        N = Integer.parseInt(st.nextToken()); // 미지의 공간 한 변 길이
        M = Integer.parseInt(st.nextToken()); // 시간의 벽 한 변의 길이
        F = Integer.parseInt(st.nextToken()); // 시간 이상 현상 개수

        machineLoc = 4; // 동(0) 서(1) 남(2) 북(3) 윗면(4) 미지평면(-1)

        // 미지의 공간의 평면도
        boolean entranceChk = false;
        int tmpX = 0, tmpY = 0;
        floorMap = new int[N][N];
        for (int i = 0; i < N; i++) {
            st = new StringTokenizer(br.readLine());
            for (int j = 0; j < N; j++) {
                floorMap[i][j] = Integer.parseInt(st.nextToken());
                if (floorMap[i][j] == 4) { // 탈출구
                    endY = i;
                    endX = j;
                }

                if (!entranceChk && floorMap[i][j] == 3) {
                    entranceChk = true;
                    tmpY = i;
                    tmpX = j;
                }
            }
        }

        // 시간의 벽 입구에 들어가는 방향 (동0 서1 남2 북3)
        entranceDir = 0;
        entranceX = 0; // 2차원
        entranceY = 0; // 2차원
        wallEndX = 0; // 3차원
        wallEndY = M-1; // 3차원, 제일 아래 행에 위치할 테니까 항상 성립
        for (int i = tmpY-1; i <= tmpY + M; i++) {
            for (int j = tmpX-1; j <= tmpX + M; j++) {
                if(!chkBoundary(i,j,N)) continue;
                if (floorMap[i][j] == 0) {
                    if (i == tmpY-1) {
                        entranceDir = 3;
                        wallEndX = (M-1) - (j - tmpX);
                    } else if (i == tmpY+M) {
                        entranceDir = 2;
                        wallEndX = (j - tmpX);
                    } else if (j == tmpX-1) {
                        entranceDir = 1;
                        wallEndX = (i - tmpY);
                    } else {
                        entranceDir = 0;
                        wallEndX = (M-1) - (i - tmpY);
                    }

                    entranceY = i;
                    entranceX = j;
                }
            }
        }

        // 시간의 벽의 단면도
        timeMap = new int[5][M][M]; // 동 서 남 북 윗면
        for (int i = 0; i < 5; i++) {
            for (int r = 0; r < M; r++) {
                st = new StringTokenizer(br.readLine());
                for (int c = 0; c < M; c++) {
                    timeMap[i][r][c] = Integer.parseInt(st.nextToken());
                    if (timeMap[i][r][c] == 2) { // 타임머신 위치 (윗면 i == 4)
                        machineY = r;
                        machineX = c;
                    }
                }
            }
        }

        anomaly = new ArrayList<>();
        vMap = new int[N][N]; // 시간 표시
        for (int i = 0; i < F; i++) {
            st = new StringTokenizer(br.readLine());
            int r = Integer.parseInt(st.nextToken());
            int c = Integer.parseInt(st.nextToken());
            int d = Integer.parseInt(st.nextToken());
            int v = Integer.parseInt(st.nextToken());
            anomaly.add(new int[]{r,c,d,v});
            vMap[r][c] = 1;
        }

        boolean chk = true;
        // 1. 3차원 이동
        int dist3d = move3d();
        if (dist3d == -1) chk = false;
        ans = dist3d;

        if (chk) {
            // 2. 이상 현상 미리 적용
            timeAnomaly();

            // 3. 2차원 이동
            int dist2d = move2d(ans + 1); // 시간 전달
            if (dist2d == -1) chk = false;
            ans += 1 + dist2d;
        }

        if (!chk) bw.write(String.valueOf(-1));
        else bw.write(String.valueOf(ans));
        bw.flush();
        bw.close();
    }

    private static int move2d(int time) {
        Queue<int[]> myqueue = new LinkedList<>();
        myqueue.add(new int[]{entranceY, entranceX, 0, time}); // i j distance time

        boolean[][] visited = new boolean[N][N];
        visited[entranceY][entranceX] = true;

        while (!myqueue.isEmpty()) {
            int[] now = myqueue.poll();
            int nowY = now[0];
            int nowX = now[1];
            int nowDist = now[2];
            int nowTime = now[3];

            if (nowY == endY && nowX == endX) {
                return nowDist;
            }

            for (int k = 0; k < 4; k++) {
                int nextY = nowY + dy[k];
                int nextX = nowX + dx[k];

                if (chkBoundary(nextY, nextX, N) && !visited[nextY][nextX]) {
                    if (floorMap[nextY][nextX] != 1 && nowTime+1 < vMap[nextY][nextX]) {
                        visited[nextY][nextX] = true;
                        myqueue.add(new int[]{nextY, nextX, nowDist+1, nowTime+1});
                    }
                }
            }
        }



        return -1;
    }

    private static void timeAnomaly() {
        for (int i = 0; i < anomaly.size(); i++) { // r c d v
            int nowY = anomaly.get(i)[0];
            int nowX = anomaly.get(i)[1];
            int dir = anomaly.get(i)[2];
            int v = anomaly.get(i)[3];

            for (int j = 0; j < N; j++) {

                int nextY = nowY + dy[dir];
                int nextX = nowX + dx[dir];
                int nextV = v * (j+1);

                if (!chkBoundary(nextY, nextX, N)) break;
                if (floorMap[nextY][nextX] == 4 || floorMap[nextY][nextX] == 3) break;

                if (vMap[nextY][nextX] != 0 && vMap[nextY][nextX] > nextV) {
                    vMap[nextY][nextX] = nextV;
                    nowY = nextY;
                    nowX = nextX;
                    continue;
                }

                vMap[nextY][nextX] = nextV;
                nowY = nextY;
                nowX = nextX;
            }
        }

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (vMap[i][j] == 0) vMap[i][j] = Integer.MAX_VALUE;
            }
        }
    }

    private static int move3d() {
        Queue<int[]> myqueue = new LinkedList<>();
        myqueue.add(new int[]{machineLoc, machineY, machineX, 0});

        boolean[][][] visited = new boolean[5][M][M];
        visited[machineLoc][machineY][machineX] = true;

        while (!myqueue.isEmpty()) {
            int[] now = myqueue.poll();
            int nowDir = now[0];
            int nowY = now[1];
            int nowX = now[2];
            int nowDist = now[3];

            if (nowDir == entranceDir && nowY == wallEndY && nowX == wallEndX) {
                return nowDist;
            }

            for (int k = 0; k < 4; k++) {
                int nextDir = nowDir;
                int nextY = nowY + dy[k];
                int nextX = nowX + dx[k];

                if (nextY < 0) { // 위쪽으로 범위 이탈
                    if (nowDir == 0) { // 현재 동쪽에 있음 -> 위
                        nextDir = 4;
                        nextY = (M-1) - nowX;
                        nextX = M-1;
                    } else if (nowDir == 1) { // 서 -> 위
                        nextDir = 4;
                        nextY = nowX;
                        nextX = 0;
                    } else if (nowDir == 2) { // 남 -> 위
                        nextDir = 4;
                        nextY = M-1;
                        nextX = nowX;
                    } else if (nowDir == 3) { // 북 -> 위
                        nextDir = 4;
                        nextY = 0;
                        nextX = (M-1) - nowX;
                    } else { // 위 -> 북
                        nextDir = 3;
                        nextY = 0;
                        nextX = (M-1) - nowX;
                    }
                } else if (nextY >= M) { // 아래쪽으로 범위 이탈
                    if (nowDir != 4) continue;
                    // 위 -> 남(2)
                    nextDir = 2;
                    nextY = 0;
                    nextX = nowX;
                } else if (nextX >= M) { // 오른쪽으로 범위 이탈
                    if (nowDir == 0) { // 동 -> 북(3)
                        nextDir = 3;
                        nextY = nowY;
                        nextX = 0;
                    } else if (nowDir == 1) { // 서 -> 남(2)
                        nextDir = 2;
                        nextY = nowY;
                        nextX = 0;
                    } else if (nowDir == 2) { // 남 -> 동(0)
                        nextDir = 0;
                        nextY = nowY;
                        nextX = 0;
                    } else if (nowDir == 3) { // 북 -> 서(1)
                        nextDir = 1;
                        nextY = nowY;
                        nextX = 0;
                    } else { // 위 -> 동(0)
                        nextDir = 0;
                        nextY = 0;
                        nextX = (M-1) - nowY;
                    }
                } else if (nextX < 0) { // 왼쪽으로 범위 이탈
                    if (nowDir == 0) { // 동 -> 남(2)
                        nextDir = 2;
                        nextY = nowY;
                        nextX = M-1;
                    } else if (nowDir == 1) { // 서 -> 북(3)
                        nextDir = 3;
                        nextY = nowY;
                        nextX = M-1;
                    } else if (nowDir == 2) { // 남 -> 서(1)
                        nextDir = 1;
                        nextY = nowY;
                        nextX = M-1;
                    } else if (nowDir == 3) { // 북 -> 동(0)
                        nextDir = 0;
                        nextY = nowY;
                        nextX = M-1;
                    } else { // 위 -> 서(1)
                        nextDir = 1;
                        nextY = 0;
                        nextX = nowY;
                    }
                }

                if (!visited[nextDir][nextY][nextX] && timeMap[nextDir][nextY][nextX] == 0) {
                    visited[nextDir][nextY][nextX] = true;
                    myqueue.add(new int[]{nextDir, nextY, nextX, nowDist+1});
                }
            }
        }

        return -1;
    }

    private static boolean chkBoundary(int i, int j, int size) {
        return (0 <= i && i < size && 0 <= j && j < size);
    }
}