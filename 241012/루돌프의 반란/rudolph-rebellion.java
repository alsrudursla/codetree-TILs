import java.io.*;
import java.util.*;

public class Main{
    static int[] dy = {-1, -1, 0, 1, 1, 1, 0, -1};
    static int[] dx = {0, -1, -1, -1, 0, 1, 1, 1};
    static int[] santaDy = {-1, 0, 1, 0}; // 상우하좌
    static int[] santaDx = {0, 1, 0, -1};
    static int N, M, P, C, D;
    static int Ry, Rx; // 루돌프 위치 좌표
    static Santa[] santaList; // 산타 좌표
    static int[][] map;
    static boolean[][] visited;
    static int[] score;
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        StringTokenizer st = new StringTokenizer(br.readLine());
        N = Integer.parseInt(st.nextToken()); // 게임판 크기
        M = Integer.parseInt(st.nextToken()); // 게임 턴 수
        P = Integer.parseInt(st.nextToken()); // 산타 수
        C = Integer.parseInt(st.nextToken()); // 루돌프의 힘 (루돌프의 충돌 시 C 만큼 산타가 밀려남)
        D = Integer.parseInt(st.nextToken()); // 산타의 힘 (산타의 충돌 시 D 만큼 산타가 밀려남)

        map = new int[N+1][N+1];
        visited = new boolean[N+1][N+1];
        score = new int[P+1];

        st = new StringTokenizer(br.readLine());
        Ry = Integer.parseInt(st.nextToken());
        Rx = Integer.parseInt(st.nextToken());

        santaList = new Santa[P+1];
        for (int i = 1; i <= P; i++) {
            st = new StringTokenizer(br.readLine());
            int number = Integer.parseInt(st.nextToken());
            int si = Integer.parseInt(st.nextToken());
            int sj = Integer.parseInt(st.nextToken());
            santaList[number] = new Santa(number, si, sj, 0, 0);
            map[si][sj] = number;
            visited[si][sj] = true;
        }

        for (int i = 0; i < M; i++) { // M 번 진행
            // 1. 루돌프 이동
            Rmove();

            // 2. 산타 이동
            Smove();

            // 산타 존재 체크
            boolean santaChk = false;
            for (int s = 1; s <= P; s++) {
                if (santaList[s] != null) { // 한 명이라도 null 이 아니면
                    santaChk = true;
                }
            }
            if (!santaChk) { // 모두 null 이면 그 즉시 게임 종료
                break;
            }

            // 기절 관리 + 산타 점수 추가
            for (int s = 1; s <= P; s++) {
                if (santaList[s] == null) continue;
                if (santaList[s].faint != 0) {
                    santaList[s].faint--;
                }
                santaList[s].score++;
            }

            // 마지막 턴까지 산타가 있으면
            if (i == M-1) {
                for (int s = 1; s <= P; s++) {
                    if (santaList[s] != null) {
                        score[s] = santaList[s].score;
                    }
                }
            }
        }

        // 출력
        for (int i = 1; i <= P; i++) {
            bw.write(score[i] + " ");
        }
        bw.flush();
        bw.close();
    }

    // 루돌프 이동
    private static void Rmove() {
        // 1. 산타마다 거리 계산
        // 2. 가장 가까운 산타가 2명 이상이라면, r 좌표가 큰 산타를 향해 돌진합니다. r이 동일한 경우, c 좌표가 큰 산타를 향해 돌진
        // 3. 가장 가까운 산타를 향해 1칸 돌진

        PriorityQueue<int[]> Rpq = new PriorityQueue<>((o1, o2) ->
                // 1. 거리(오름차순) -> 2. r(내림차순) -> 3. c(내림차순)
                // [ 산타 번호, 거리, 행(r), 열(c) ]
                o1[1] != o2[1] ? Integer.compare(o1[1],o2[1]) :
                        (o1[2] != o2[2] ? Integer.compare(o2[2], o1[2]) : Integer.compare(o2[3], o1[3])));

        for (int i = 1; i <= P; i++) {
            if (santaList[i] == null) continue;
            int tmp1 = (Ry - santaList[i].i) * (Ry - santaList[i].i);
            int tmp2 = (Rx - santaList[i].j) * (Rx - santaList[i].j);
            int distance =  tmp1 + tmp2;
            Rpq.add(new int[]{santaList[i].number, distance, santaList[i].i, santaList[i].j});
        }

        int[] closest_santa = Rpq.poll();
        if (closest_santa == null) return;
        int sNumber = closest_santa[0];
        int si = closest_santa[2];
        int sj = closest_santa[3];

        // 방향 선택하기
        int min_distance = Integer.MAX_VALUE;
        int[] min_direction = {0,0};
        for (int k = 0; k < 8; k++) {
            int next_i = Ry + dy[k];
            int next_j = Rx + dx[k];

            if (1 <= next_i && next_i <= N && 1 <= next_j && next_j <= N) {
                int tmp = (si - next_i) * (si - next_i) + (sj - next_j) * (sj - next_j);
                if (tmp < min_distance) {
                    min_distance = tmp;
                    min_direction[0] = dy[k];
                    min_direction[1] = dx[k];
                }
            }
        }

        int new_i = Ry + min_direction[0];
        int new_j = Rx + min_direction[1];

        // 충돌
        if (visited[new_i][new_j]) { // 움직인 좌표에 산타가 있음
            if (santaList[sNumber] == null) return;
            santaList[sNumber].score += C;
            visited[si][sj] = false;
            map[si][sj] = 0;

            for (int s = 0; s < C; s++) { // 산타 밀려남
                santaList[sNumber].i += min_direction[0];
                santaList[sNumber].j += min_direction[1];
            }
            interaction(sNumber, santaList[sNumber].i, santaList[sNumber].j, min_direction[0], min_direction[1]); // 상호작용

            // 루돌프와 충돌한 산타 기절
            if (santaList[sNumber] != null) {
                santaList[sNumber].faint = 2;
            }
        }

        // 루돌프 좌표 저장
        Ry += min_direction[0];
        Rx += min_direction[1];
    }

    // 산타 이동
    private static void Smove() {
        for (int i = 1; i <= P; i++) {
            if (santaList[i] == null) continue;
            if (santaList[i].faint != 0) continue;

            // 방향 선택하기
            int now_distance = (Ry - santaList[i].i) * (Ry - santaList[i].i) + (Rx - santaList[i].j) * (Rx - santaList[i].j);
            int min_distance = now_distance;
            int[] min_direction = {0, 0};
            for (int k = 0; k < 4; k++) {
                int next_i = santaList[i].i + santaDy[k];
                int next_j = santaList[i].j + santaDx[k];

                if (1 <= next_i && next_i <= N && 1 <= next_j && next_j <= N) {
                    if (!visited[next_i][next_j]) {
                        // 다른 산타가 없어야만 갈 수 있다
                        int tmp = (Ry - next_i) * (Ry - next_i) + (Rx - next_j) * (Rx - next_j);
                        if (tmp < min_distance) {
                            min_distance = tmp;
                            min_direction[0] = santaDy[k];
                            min_direction[1] = santaDx[k];
                        }
                    }
                }
            }

            // 움직일 수 있는 칸이 없다
            if (min_distance == Integer.MAX_VALUE) continue;

            // 루돌프로부터 가까워질 수 있는 방법이 없다 (거리가 같거나 더 멀어짐)
            if (min_distance >= now_distance) continue;

            // 움직일 수 있으면 산타 이동
            map[santaList[i].i][santaList[i].j] = 0;
            visited[santaList[i].i][santaList[i].j] = false;
            int new_i = santaList[i].i + min_direction[0];
            int new_j = santaList[i].j + min_direction[1];
            // 루돌프와 충돌
            if ((Ry == new_i) && (Rx == new_j)) {
                santaList[i].score += D;

                for (int s = 0; s < D - 1; s++) { // 밀려난 거리 = D - 루돌프한테 전진한 횟수(1)
                    santaList[i].i -= min_direction[0];
                    santaList[i].j -= min_direction[1];
                }

                if ((D-1) != 0) { // 제자리가 아닐 경우에만 실행
                    interaction(santaList[i].number, santaList[i].i, santaList[i].j, -min_direction[0], -min_direction[1]);
                } else { // 제자리
                    visited[santaList[i].i][santaList[i].j] = true;
                }

                // 루돌프와 충돌한 산타 기절
                if (santaList[i] != null) santaList[i].faint = 2;

            } else {
                // 충돌 안함, 그 자리에 픽스
                map[new_i][new_j] = santaList[i].number;
                visited[new_i][new_j] = true;

                santaList[i].i += min_direction[0];
                santaList[i].j += min_direction[1];
            }
        }
    }

    // 상호작용
    private static void interaction(int sn, int si, int sj, int dir_i, int dir_j) {
        // 산타는 충돌 후 착지하게 되는 칸에 다른 산타가 있다면 그 산타는 1칸 해당 방향으로 밀려나

        if (santaInRange(sn)) {
            if (visited[si][sj]) {
                int now_santa = map[si][sj]; // 맞은 다른 산타
                map[si][sj] = sn; // 날라온 산타가 그 자리에 착지

                santaList[now_santa].i += dir_i;
                santaList[now_santa].j += dir_j;
                if (santaInRange(now_santa)) { // 범위 안에 있으면
                    if (visited[santaList[now_santa].i][santaList[now_santa].j]) {
                        // 또 누가 있으면, 다시 상호작용 코드
                        interaction(now_santa, santaList[now_santa].i, santaList[now_santa].j, dir_i, dir_j);
                    } else {
                        // 아무도 없었음
                        map[santaList[now_santa].i][santaList[now_santa].j] = now_santa;
                        visited[santaList[now_santa].i][santaList[now_santa].j] = true;
                    }
                }
            } else {
                // 아무도 없었음
                map[si][sj] = sn;
                visited[si][sj] = true;
            }
        }
    }

    // 범위 확인
    private static boolean santaInRange(int sNumber) {
        if (santaList[sNumber].i < 1 || santaList[sNumber].i > N || santaList[sNumber].j < 1 || santaList[sNumber].j > N) {
            score[sNumber] = santaList[sNumber].score; // 점수 저장
            santaList[sNumber] = null; // 산타 삭제
            return false;
        } else {
            return true;
        }
    }

    static class Santa {
        int number;
        int i;
        int j;
        int score;
        int faint;

        Santa(int number, int i, int j, int score, int faint) {
            this.number = number;
            this.i = i;
            this.j = j;
            this.score = score;
            this.faint = faint;
        }
    }
}