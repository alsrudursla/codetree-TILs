import java.io.*;
import java.util.*;

public class Main {
    static int[] dy = {-1, 0, 1, 0};
    static int[] dx = {0, -1, 0, 1};
    static int K, M;
    static int[][] map = new int[5][5];
    static PriorityQueue<Value> pq;
    static int[] wall;
    static Queue<int[]> piece_arr; // 삭제할 조각 위치 배열
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        StringTokenizer st = new StringTokenizer(br.readLine());
        K = Integer.parseInt(st.nextToken()); // 반복 횟수
        M = Integer.parseInt(st.nextToken()); // 벽면의 유물 개수

        for (int i = 0; i < 5; i++) {
            st = new StringTokenizer(br.readLine());
            for (int j = 0; j < 5; j++) {
                map[i][j] = Integer.parseInt(st.nextToken());
            }
        }

        // 벽면의 유물 조각 번호
        wall = new int[M];
        st = new StringTokenizer(br.readLine());
        for (int i = 0; i < M; i++) {
            wall[i] = Integer.parseInt(st.nextToken());
        }

        /*
        아직 K번의 턴을 진행하지 못했지만, 탐사 진행 과정에서 어떠한 방법을 사용하더라도 유물을 획득할 수 없었다면 모든 탐사는 그 즉시 종료됩니다.
        이 경우 얻을 수 있는 유물이 존재하지 않음으로, 종료되는 턴에 아무 값도 출력하지 않음에 유의합니다.
         */
        pq = new PriorityQueue<>((o1, o2) ->
                // [ 가치(내림차순), 각도(오름차순), 열(오름차순), 행(오름차순) ]
                o1.total_value != o2.total_value ? Integer.compare(o2.total_value, o1.total_value) :
                        (o1.selected_angle != o2.selected_angle ? Integer.compare(o1.selected_angle, o2.selected_angle) :
                                (o1.j != o2.j ? Integer.compare(o1.j, o2.j) :
                                        Integer.compare(o1.i, o2.i))));

        for (int i = 0; i < K; i++) { // K 번 반복
            for (int small_i = 1; small_i <= 3; small_i++) { // 회전하는 작은 맵이 갈 수 있는 범위
                for (int small_j = 1; small_j <= 3; small_j++) {
                    rotate(small_i, small_j); // 3번 회전해보고, pq 에 값 저장
                }
            }

            Value value = pq.poll();
            if (value == null) return;
            int pq_value = value.total_value;
            if (pq_value == 0) {
                break;
            } else {
                bw.write(String.valueOf(pq_value + " "));
            }

            // 벽면 숫자 업데이트
            for (int a = 0; a < value.tmp_wall.length; a++) {
                wall[a] = value.tmp_wall[a];
            }

            // 맵 업데이트
            for (int a = 0; a < 5; a++) {
                for (int b = 0; b < 5; b++) {
                    map[a][b] = value.new_map[a][b];
                }
            }

            pq.clear();
        }

        bw.flush();
        bw.close();
        

        // 1. 회전하기 (중심 좌표를 다 해봐야 함)
        // 시계 방향 90, 180, 270 다 해보고, 유물 가치 제일 큰 값 선택 (bfs)
            // 같은 번호가 3개 이상 -> 조각 개수가 유물 가치 -> 사라짐 -> 벽면의 숫자가 새겨짐
            // 새로운 조각은 (1) 열 번호가 작은 순으로 조각이 생겨납니다. 만약 열 번호가 같다면 (2) 행 번호가 큰 순으로 조각이 생겨납니다
            // 단, 벽면의 숫자는 충분히 많이 적혀 있어 생겨날 조각의 수가 부족한 경우는 없다고 가정
        // 가치가 같으면, 회전한 각도가 가장 작은 방법 -> 회전 중심 좌표의 열이 가장 작은 구간 -> 열이 같다면 행이 가장 작은 구간

        // pq 에 value[][] 를 넣어서, 1. 가치 비교 2. 각도 비교 3. 열 비교 4. 행 비교
    }

    // 3번 회전해보고, pq 에 값 저장
    private static void rotate(int i, int j) {
        PriorityQueue<Treasure> treasures = new PriorityQueue<>((o1, o2) ->
                o1.value != o2.value ? Integer.compare(o2.value, o1.value) : Integer.compare(o1.angle, o2.angle));

        // 기존 맵은 변경하지 않는다
        int[][] map_copy = new int[5][5];
        int[][] map_copy1 = new int[5][5];;
        int[][] map_copy2 = new int[5][5];;
        int[][] map_copy3 = new int[5][5];;
        for (int a = 0; a < 5; a++) {
            for (int b = 0; b < 5; b++) {
                map_copy1[a][b] = map[a][b];
                map_copy2[a][b] = map[a][b];
                map_copy3[a][b] = map[a][b];
            }
        }
        Queue<Integer> wall_copy = new LinkedList<>();
        for (int a = 0; a < wall.length; a++) {
            wall_copy.add(wall[a]);
        }
        piece_arr = new LinkedList<>();
        int rotate_value = 0;

        // 회전할 맵에 값 저장
        int[][] smallMap = new int[3][3];
        int map_i = i - 1;
        int map_j = j - 1;
        for (int small_i = 0; small_i < 3; small_i++) {
            for (int small_j = 0; small_j < 3; small_j++) {
                smallMap[small_i][small_j] = map[map_i][map_j];

                map_j++;

                if (map_j == j + 2) {
                    map_j = j - 1;
                    map_i++;
                }
            }
        }

        // 1. 90 도 회전 (시계)
        int[][] small_90 = new int[3][3];
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                small_90[x][3 - y - 1] = smallMap[y][x];
            }
        }

        // 원래 맵에서 회전된 값 저장
        rotateAndApply(map_copy1, small_90, i, j);

        // 같은 숫자 3개 이상이면 유물 가치로 선택
        rotate_value = bfs(map_copy1, 90);
        treasures.add(new Treasure(rotate_value, 90));

        // 2. 180 도 회전
        int[][] small_180 = new int[3][3];
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                small_180[3 - y - 1][3 - x - 1] = smallMap[y][x];
            }
        }

        rotateAndApply(map_copy2, small_180, i, j);
        rotate_value = bfs(map_copy2, 180);
        treasures.add(new Treasure(rotate_value, 180));

        // 3. 270 도 회전
        int[][] small_270 = new int[3][3];
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                small_270[3 - x - 1][y] = smallMap[y][x];
            }
        }

        rotateAndApply(map_copy3, small_270, i, j);
        rotate_value = bfs(map_copy3, 270);
        treasures.add(new Treasure(rotate_value, 270));

        // 4. 3개 가치 비교 -> 각도 비교 -> pq 에 저장
        // [ 가치, 각도, 열, 행 ]
        Treasure selected = treasures.poll();
        assert selected != null;
        int total_value = selected.value;
        int selected_angle = selected.angle;

        switch (selected_angle) {
            case 90:
                map_copy = map_copy1;
                break;
            case 180:
                map_copy = map_copy2;
                break;
            case 270:
                map_copy = map_copy3;
                break;
        }
        // 더이상 안나올 때까지 진행해야 함!!
        // 1. piece 배열 [0] 이 현재 selected angle 인 것만 0 으로 삭제
        // 벽면의 유물 번호로 바꾸기
        // bfs()
        // 더 이상 piece 배열 [0] 에 selected angle 이 없을 때까지 진행

        PriorityQueue<int[]> change_arr = new PriorityQueue<>(((o1, o2) ->
                o1[1] != o2[1] ? Integer.compare(o1[1], o2[1]) : Integer.compare(o2[0], o1[0])));

        while (!piece_arr.isEmpty()) {

            while (!piece_arr.isEmpty()) {
                int[] now_piece = piece_arr.poll();
                if (now_piece[2] == selected_angle) {
                    map_copy[now_piece[0]][now_piece[1]] = 0;
                    change_arr.add(new int[]{now_piece[0], now_piece[1]});
                }
            }

            while (!change_arr.isEmpty()) {
                int[] now_change_arr = change_arr.poll();
                Integer wall_number = wall_copy.poll();
                if (wall_number == null) return;

                map_copy[now_change_arr[0]][now_change_arr[1]] = wall_number;
            }

            total_value += bfs(map_copy, selected_angle);
        }

        // 현재 남은 벽면 숫자도 같이 전해주기
        int[] tmp_wall = new int[wall_copy.size()];
        int idx = 0;
        while (!wall_copy.isEmpty()) {
            int tmp_wall_v = wall_copy.poll();
            tmp_wall[idx++] = tmp_wall_v;
        }

        pq.add(new Value(total_value, selected_angle, j, i, tmp_wall, map_copy));
    }

    private static void rotateAndApply(int[][] map_copy, int[][] small_90, int i, int j) {
        int small90_i = 0;
        int small90_j = 0;
        for (int small_i = i-1; small_i <= i+1; small_i++) {
            for (int small_j = j-1; small_j <= j+1; small_j++) {
                map_copy[small_i][small_j] = small_90[small90_i][small90_j];

                small90_j++;

                if (small90_j == 3) {
                    small90_j = 0;
                    small90_i++;
                }
            }
        }
    }

    private static int bfs(int[][] map_copy, int angle) {
        // 같은 숫자 3개 이상이면 유물 가치로 선택
        Queue<int[]> myqueue = new LinkedList<>();
        boolean[][] visited = new boolean[5][5];
        int tmp_value = 0;
        for (int l = 0; l < 5; l++) {
            for (int m = 0; m < 5; m++) {
                if (!visited[l][m]) {
                    int now_number = map_copy[l][m];
                    myqueue.add(new int[]{l, m});

                    int piece = 1;
                    Queue<int[]> tmp = new LinkedList<>();
                    tmp.add(new int[]{l,m});

                    while (!myqueue.isEmpty()) {
                        int[] now = myqueue.poll();
                        int now_i = now[0];
                        int now_j = now[1];
                        visited[now_i][now_j] = true;

                        for (int k = 0; k < 4; k++) {
                            int next_i = now_i + dy[k];
                            int next_j = now_j + dx[k];
                            if (0 <= next_i && next_i < 5 && 0 <= next_j && next_j < 5) {
                                if (map_copy[next_i][next_j] == now_number && !visited[next_i][next_j]) {
                                    visited[next_i][next_j] = true;
                                    piece++;
                                    myqueue.add(new int[]{next_i, next_j});
                                    tmp.add(new int[]{next_i, next_j});
                                }
                            }
                        }
                    }

                    // 3 이상이 안되는 조각 위치는 지워야 되는데!!!
                    if (piece >= 3) {
                        tmp_value += piece;
                        while (!tmp.isEmpty()) {
                            int[] tmp_v = tmp.poll();
                            piece_arr.add(new int[]{tmp_v[0], tmp_v[1], angle, piece});
                        }
                    }
                }
            }
        }

        return tmp_value;
    }
    
    static class Treasure{
        int value;
        int angle;
        Treasure(int value, int angle) {
            this.value = value;
            this.angle = angle;
        }
    }

    static class Value {
        int total_value;
        int selected_angle;
        int j;
        int i;
        int[] tmp_wall;
        int[][] new_map;
        Value(int total_value, int selected_angle, int j, int i, int[] tmp_wall, int[][] new_map) {
            this.total_value = total_value;
            this.selected_angle = selected_angle;
            this.j = j;
            this.i = i;
            this.tmp_wall = tmp_wall;
            this.new_map = new_map;
        }
    }
}