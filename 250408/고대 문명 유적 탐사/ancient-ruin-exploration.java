import java.io.*;
import java.util.*;
public class Main {
    static int treasure, tmp_treasure, angle, tmp_angle;
    static int[][] map;
    static boolean[][] visited;
    static List<Integer> pieces;
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        StringTokenizer st = new StringTokenizer(br.readLine());
        int K = Integer.parseInt(st.nextToken()); // 탐사 반복 횟수
        int M = Integer.parseInt(st.nextToken()); // 벽면의 유물 조각 개수

        map = new int[5][5];
        for (int i = 0; i < 5; i++) {
            st = new StringTokenizer(br.readLine());
            for (int j = 0; j < 5; j++) {
                map[i][j] = Integer.parseInt(st.nextToken());
            }        
        }

        st = new StringTokenizer(br.readLine());
        pieces = new ArrayList<>();
        for (int i = 0; i < M ; i++) {
            pieces.add(Integer.parseInt(st.nextToken()));
        }

        // 탐사는 K번 진행
        for (int t = 0; t < K; t++) {
            // 1. 회전할 3x3 설정 (9번 다 돌려봐야 됨)
            // 2. 회전 (90, 180, 270 중 1차 유물 획득 가치 제일 큰 것 && 각 작은 -> 열 작은 -> 행 작은)
            // 3. 1차 유물 획득 가치 결정할 맵으로 변환
            // 4. 벽면에 쓰인 조각 대입
            // 5. 조각이 3개 이상 연결되지 않을 때까지 유물 연쇄 획득
            // 탐사 반복
        	
            treasure = -1;
            angle = -1;
            int[][] selectedMap = new int[5][5];
            // 3x3 첫 번째 좌표로 돌기
            for (int c = 0; c < 3; c++) {
                for (int r = 0; r < 3; r++) {
                    tmp_treasure = 0;
                    tmp_angle = 0;
                    int[][] tmp_map = rotateMap(r, c);
                    
                    // 1차 유물 가치가 더 크면 맵 변환
                    if (treasure <= tmp_treasure) {
                    	if (treasure == tmp_treasure) {
                    		if (tmp_angle >= angle) continue;
                    	}
                    	
                        treasure = tmp_treasure;
                        angle = tmp_angle;
                        for (int i = 0; i < 5; i++) {
                            for (int j = 0; j < 5; j++) {
                                selectedMap[i][j] = tmp_map[i][j];
                            }                        
                        }
                    }
                }
             }
            
            if (treasure == 0) break;

             for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) map[i][j] = selectedMap[i][j];
             }
             
             // 벽면 숫자 대입 (영원히 반복해 ^^)
             while (true) {
                // 1. 벽면 숫자 대입
                // 2. 유물 획득
                writeNewNumber();
                
                int sum = getTreasure(map);
                if (sum == 0) break;
                
            	erase_treasure(map);
            	treasure += sum;
             }

            if (treasure == 0) break;
            else bw.write(treasure + " ");
            bw.flush();
        }
        bw.close();
    }
    
    private static void writeNewNumber() {
    	for (int j = 0; j < 5; j++) {
    		for (int i = 4; i >= 0; i--) {
    			if (map[i][j] == 0) {
    				map[i][j] = pieces.get(0);
    				pieces.remove(0);
    			}
    		}
    	}
    }

    private static int[][] rotateMap(int startR, int startC) {
        int val = -1;
        int ang = -1;
        int[][] selected_rMap = new int[5][5];
        for (int k = 1; k <= 3; k++) {
            // 3번 회전 : 90, 180, 270

            // 회전할 맵 만들기 (회전 전)
            int[][] tmp_rMap = new int[3][3];
            for (int i = startR; i < startR+3; i++) {
                for (int j = startC; j < startC+3; j++) {
                    tmp_rMap[i-startR][j-startC] = map[i][j];
                }
            }

            // 1. 회전
            int[][] tmp = rotate(k, tmp_rMap);

            // 2. 다시 5x5 맵에 대입
            // 기존 맵 변형하지 않기 위해 복사하기
            int[][] copyMap = new int[5][5];
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    copyMap[i][j] = map[i][j];
                }
            }

             for (int i = startR; i < startR+3; i++) {
                for (int j = startC; j < startC+3; j++) {
                    copyMap[i][j] = tmp[i-startR][j-startC];
                }
             }

            // 3. 1차 유물 획득 가치 계산
            int tmp_val = getTreasure(copyMap);

            // 4. 비교
            if (val < tmp_val) {
                val = tmp_val;
                ang = k;
                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 5; j++) {
                        selected_rMap[i][j] = copyMap[i][j];
                    }
                }
            }
        }

        // 없어진 유물은 0으로 만들기
        erase_treasure(selected_rMap);

        // 1차 유물 가치
        tmp_treasure = val;
        
        // 각도
        if (ang == 1) tmp_angle = 90;
        else if (ang == 2) tmp_angle = 180;
        else tmp_angle = 270;

        return selected_rMap; // 1차 유물 가치가 제일 큰 각도의 맵 반환
    }
    
    private static void erase_treasure(int[][] sMap) {
    	int[] dy = {-1, 0, 1, 0};
    	int[] dx = {0, 1, 0, -1};
    	boolean[][] v = new boolean[5][5];
    	for (int i = 0; i < 5; i++) {
    		for (int j = 0; j < 5; j++) {
    			if (v[i][j]) continue;
    			v[i][j] = true;
    			
    			int size = 1;
    	    	Queue<int[]> myqueue = new LinkedList<>();
    	    	myqueue.add(new int[] {i, j});
    	    	
    	    	List<int[]> path = new ArrayList<>();
    	    	path.add(new int[] {i, j});
    	    	
    	    	while (!myqueue.isEmpty()) {
    	    		int[] now = myqueue.poll();
    	    		int nowY = now[0];
    	    		int nowX = now[1];
    	    		
    	    		for (int k = 0; k < 4; k++) {
    	    			int nextY = nowY + dy[k];
    	    			int nextX = nowX + dx[k];
    	    			
    	    			if (0 <= nextY && nextY < 5 && 0 <= nextX && nextX < 5) {
    	    				if (!v[nextY][nextX] && sMap[nextY][nextX] == sMap[i][j]) {
    	    					v[nextY][nextX] = true;
    	    					myqueue.add(new int[] {nextY, nextX});
    	    					path.add(new int[] {nextY, nextX});
    	    					size++;
    	    				}
    	    			}
    	    		}
    	    	}
    	    	
    	    	if (size >= 3) {
    	    		for (int[] p : path) {
    	    			sMap[p[0]][p[1]] = 0;
    	    		}
    	    	}
    		}
    	}
    }
    
    private static int getTreasure(int[][] cMap) {
    	int val = 0;
    	visited = new boolean[cMap.length][cMap[0].length];
    	for (int i = 0; i < 5; i++) {
    		for (int j = 0; j < 5; j++) {
    			if (visited[i][j]) continue;
    			
    			val += bfs(i, j, cMap);
    		}
    	}
    	return val;
    }
    
    private static int bfs(int i, int j, int[][] cMap) {
    	int[] dy = {-1, 0, 1, 0};
    	int[] dx = {0, 1, 0, -1};
    	int size = 1;
    	Queue<int[]> myqueue = new LinkedList<>();
    	myqueue.add(new int[] {i, j});
    	visited[i][j] = true;
    	
    	while (!myqueue.isEmpty()) {
    		int[] now = myqueue.poll();
    		int nowY = now[0];
    		int nowX = now[1];
    		
    		for (int k = 0; k < 4; k++) {
    			int nextY = nowY + dy[k];
    			int nextX = nowX + dx[k];
    			
    			if (0 <= nextY && nextY < 5 && 0 <= nextX && nextX < 5) {
    				if (!visited[nextY][nextX] && cMap[nextY][nextX] == cMap[i][j]) {
    					visited[nextY][nextX] = true;
    					myqueue.add(new int[] {nextY, nextX});
    					size++;
    				}
    			}
    		}
    	}
    	
    	if (size >= 3) return size;
    	else return 0;
    }

    private static int[][] rotate(int cnt, int[][] rMap) {
        // cnt 횟수만큼 90도씩 회전
        int[][] newMap = new int[rMap.length][rMap[0].length];
        for (int i = 0; i < cnt; i++) {
            for (int r = 0; r < rMap.length; r++) {
                for (int c = 0; c < rMap[0].length; c++) {
                    newMap[r][rMap.length - 1- c] = rMap[c][r];
                }
            }
            
            // 다시 회전할 경우를 대비해 기존 map 을 회전한 newMap 으로 업데이트
            for (int r = 0; r < rMap.length; r++) {
                for (int c = 0; c < rMap[0].length; c++) {
                	rMap[r][c] = newMap[r][c];
                }
            }
        }
        return newMap;
    }
}