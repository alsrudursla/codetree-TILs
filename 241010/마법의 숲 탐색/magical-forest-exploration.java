import java.util.*;
import java.io.*;

public class Main {

    private static int R, C, K; // 행, 열, 골렘의 개수를 의미합니다
    private static int[][] A; // 실제 숲을 [3~R+2][0~C-1]로 사용하기위해 행은 3만큼의 크기를 더 갖습니다
    private static int[] dy = {-1, 0, 1, 0}, dx = {0, 1, 0, -1};
    private static boolean[][] isExit; // 해당 칸이 골렘의 출구인지 저장합니다
    private static int answer = 0; // 각 정령들이 도달할 수 있는 최하단 행의 총합을 저장합니다

    // (y, x)가 숲의 범위 안에 있는지 확인하는 함수입니다
    private static boolean inRange(int y, int x) {
        return 3 <= y && y < R + 3 && 0 <= x && x < C;
    }

    // 골렘의 중심이 y, x에 위치할 수 있는지 확인합니다.
    // 북쪽에서 남쪽으로 내려와야하므로 중심이 (y, x)에 위치할때의 범위와 (y-1, x)에 위치할떄의 범위 모두 확인합니다
    private static boolean canGo(int y, int x) {
        if (y+1<R+3 && 0<=x-1 && x+1<C) { // 일단 범위 안에 있음
            if (A[y-1][x-1] == 0 && A[y-1][x] == 0 && A[y-1][x+1] == 0 &&
                    A[y][x-1] == 0 && A[y][x] == 0 && A[y][x+1] == 0 &&
                    A[y+1][x] == 0) return true;
        }
        return false;
    }

    // 정령이 움직일 수 있는 모든 범위를 확인하고 도달할 수 있는 최하단 행을 반환합니다
    private static int bfs(int y, int x) {
        Queue<int[]> q = new LinkedList<>();
        q.offer(new int[]{y, x});

        boolean[][] visit = new boolean[R + 3][C];
        visit[y][x] = true;
        int result = y;

        while (!q.isEmpty()) {
            int[] now = q.poll();
            int now_y = now[0];
            int now_x = now[1];

            for (int k = 0; k < 4; k++) {
                int ny = now_y + dy[k];
                int nx = now_x + dx[k];
                // 정령의 움직임은 골렘 내부이거나
                // 골렘의 탈출구에 위치하고 있다면 다른 골렘으로 옮겨 갈 수 있습니다
                if (inRange(ny, nx) && !visit[ny][nx] &&
                        (A[ny][nx] == A[now_y][now_x] || (A[ny][nx] != 0 && isExit[now_y][now_x]))) {
                    q.offer(new int[]{ny, nx});
                    visit[ny][nx] = true;
                    result = Math.max(result, ny);
                }
            }
        }
        return result;
    }

    // 골렘id가 중심 (y, x), 출구의 방향이 d일때 규칙에 따라 움직임을 취하는 함수입니다
    // 1. 남쪽으로 한 칸 내려갑니다.
    // 2. (1)의 방법으로 이동할 수 없으면 서쪽 방향으로 회전하면서 내려갑니다.
    // 3. (1)과 (2)의 방법으로 이동할 수 없으면 동쪽 방향으로 회전하면서 내려갑니다.
    private static void down(int y, int x, int d, int id) {
        if (canGo(y + 1, x)) {
            // 아래로 내려갈 수 있는 경우입니다
            down(y + 1, x, d, id);
        } else if (canGo(y + 1, x - 1)) {
            // 왼쪽 아래로 내려갈 수 있는 경우입니다
            down(y + 1, x - 1, (d + 3) % 4, id);
        } else if (canGo(y + 1, x + 1)) {
            // 오른쪽 아래로 내려갈 수 있는 경우입니다
            down(y + 1, x + 1, (d + 1) % 4, id);
        } else {
            // 1, 2, 3의 움직임을 모두 취할 수 없을떄 입니다.
            if (!inRange(y-1, x) || !inRange(y+1, x)) {
                // 숲을 벗어나는 경우 모든 골렘이 숲을 빠져나갑니다
                for (int i = 0; i < R+3; i++) {
                    for (int j = 0; j < C; j++) {
                        A[i][j] = 0;
                        isExit[i][j] = false;
                    }
                }
            } else {
                // 골렘이 숲 안에 정착합니다
                A[y][x] = id;
                for (int k = 0; k < 4; k++)
                    A[y + dy[k]][x + dx[k]] = id;
                // 골렘의 출구를 기록하고
                isExit[y + dy[d]][x + dx[d]] = true;
                // bfs를 통해 정령이 최대로 내려갈 수 있는 행를 계산하여 누적합합니다
                answer += bfs(y, x) - 3 + 1;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        StringTokenizer st = new StringTokenizer(br.readLine());
        R = Integer.parseInt(st.nextToken());
        C = Integer.parseInt(st.nextToken());
        K = Integer.parseInt(st.nextToken());

        A = new int[R+3][C];
        isExit = new boolean[R+3][C];

        for (int i = 1; i <= K; i++) {
            st = new StringTokenizer(br.readLine());
            int start_j = Integer.parseInt(st.nextToken()) -1;
            int exit_dir = Integer.parseInt(st.nextToken());
            down(0, start_j, exit_dir, i);
        }

        bw.write(String.valueOf(answer));
        bw.newLine();
        bw.flush();
        bw.close();
    }
}