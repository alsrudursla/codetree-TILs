import java.io.*;
import java.util.*;
public class Main {
    static class Stone {
        int startC;
        int exitDir;
        Stone(int startC, int exitDir) {
            this.startC = startC;
            this.exitDir = exitDir;
        }
    }
    static List<Stone> stones;
    static int[][] map;
    static boolean[][] visited;
    static int R, C, nowR, nowC;
    static int[] dy = {-1, 0, 1, 0}; // 북 동 남 서
    static int[] dx = {0, 1, 0, -1};
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        StringTokenizer st = new StringTokenizer(br.readLine());
        R = Integer.parseInt(st.nextToken());
        C = Integer.parseInt(st.nextToken());
        int K = Integer.parseInt(st.nextToken()); // 정령의 수

        stones = new ArrayList<>();
        for (int i = 0; i < K; i++) {
            st = new StringTokenizer(br.readLine());
            int startC = Integer.parseInt(st.nextToken());
            int exitDir = Integer.parseInt(st.nextToken());
            stones.add(new Stone(startC, exitDir));
        }

        // 정령의 수 만큼 반복
        map = new int[R+2][C+1];
        visited = new boolean[R+2][C+1];
        int ans = 0;
        for (int t = 0; t < K; t++) {
        	// 처음 시작 좌표
        	nowR = 0;
            nowC = stones.get(t).startC;

            // 이동 루프
            boolean canGo = true;
            while (canGo) {
                // 1. 아래로 이동
            	goDown(t);
            	if (nowR == R) { // 맵의 끝에 도달 (맵의 끝은 R+1)
            		writeVisited(nowR, nowC);
            		break;
            	}
            	
            	// 2. 서쪽으로 이동
            	// 서쪽으로 이동했으면 다시 아래부터 시작
            	// 서쪽으로 이동 못했으면 동쪽으로 이동
            	if (chkCanGoWest(nowR, nowC)) {
            		// 좌표 이동 + 출구 반시계 회전
            		nowR++;
            		nowC--;
            		int ori_dir = stones.get(t).exitDir;
            		stones.get(t).exitDir = ((ori_dir+3)%4);
            		continue; // 다시 반복 : 아래 -> 서 -> 동
            	} else {
            		if (chkCanGoEast(nowR, nowC)) {
            			// 좌표 이동 + 출구 시계 회전
            			nowR++;
            			nowC++;
            			int ori_dir = stones.get(t).exitDir;
                		stones.get(t).exitDir = ((ori_dir+1)%4);
            			continue;
            		} else {
            			canGo = false;
            		}
            	}
            }

            // 최종 좌표가 맵 안에 없으면 맵 초기화 -> 맵 합산 없이 다음 정령으로
            if (!chkInTheMap(nowR, nowC)) {
                for (int i = 0; i <= R+1; i++) {
                    for (int j = 0; j <= C; j++) {
                        visited[i][j] = false;
                    }
                }
                continue;
            }

            // 최종 행 합산
            if (nowR == R) { // 맵의 끝은 R+1
                ans += R; // 맵 사이즈를 변경했기 때문에 마지막 위치는 R+1, 실제 마지막 행의 값은 R
            } else {
                ans += bfs(nowR, nowC, stones.get(t).exitDir) - 1;
            }
            
            // 맵 업데이트
            writeVisited(nowR, nowC);
        }

        bw.write(String.valueOf(ans));
        bw.flush();
        bw.close();
    }
    
    private static int bfs(int mY, int mX, int exitDir) {
    	// 출구가 다른 곳이랑 붙어있는지 확인 (현재 정령의 visited 아직 체크 안되어 있음)
    	// 안붙어있으면 return 현재 R+1
    	// 붙어있으면 bfs 로 이동
    	
    	// 출구 좌표
    	int exitY = mY + dy[exitDir];
    	int exitX = mX + dx[exitDir];
    	boolean canGo = false;
    	for (int k = 0; k < 4; k++) {
    		int chkY = exitY + dy[k];
    		int chkX = exitX + dx[k];
    		
    		if (chkBoundary(chkY, chkX) && visited[chkY][chkX]) {
    			// 범위 내 && 다른 골렘이 있음 -> 행 이동 가능
    			canGo = true;
    			break;
    		}
    	}
    	
    	if (!canGo) return mY + 1;
    	
    	Queue<int[]> myqueue = new LinkedList<>();
    	myqueue.add(new int[] {exitY, exitX});
    	
    	boolean[][] v = new boolean[R+2][C+1];
    	v[exitY][exitX] = true;
    	
    	int biggestY = mY + 1; // 현재 골렘에서 갈 수 있는 가장 큰 행 좌표
    	while (!myqueue.isEmpty()) {
    		int[] now = myqueue.poll();
    		int nowY = now[0];
    		int nowX = now[1];
    		
    		biggestY = Math.max(biggestY, nowY);
    		
    		for (int k = 0; k < 4; k++) {
    			int nextY = nowY + dy[k];
    			int nextX = nowX + dx[k];
    			
    			if (chkBoundary(nextY, nextX) && visited[nextY][nextX] && !v[nextY][nextX]) {
    				// 범위 내 && 다른 골렘 존재 && 가지 않았던 곳
    				v[nextY][nextX] = true;
    				myqueue.add(new int[] {nextY, nextX});
    			}
    		}
    	}
    	
    	return biggestY;
    }
    
    private static boolean chkInTheMap(int mY, int mX) {
    	if (!(2 <= mY && mY <= R+1 && 1 <= mX && mX <= C)) return false;
    	for (int k = 0; k < 4; k++) {
    		int chkY = mY + dy[k];
    		int chkX = mX + dx[k];
    		if (!(2 <= chkY && chkY <= R+1 && 1 <= chkX && chkX <= C)) return false;
    	}
    	return true;
    }
    
    private static boolean chkCanGoEast(int mY, int mX) {
    	// 5곳 확인
    	if (!chkBoundary(mY-1, mX+1) || visited[mY-1][mX+1]) return false;
    	if (!chkBoundary(mY, mX+2) || visited[mY][mX+2]) return false;
    	if (!chkBoundary(mY+1, mX+2) || visited[mY+1][mX+2]) return false;
    	if (!chkBoundary(mY+1, mX+1) || visited[mY+1][mX+1]) return false;
    	if (!chkBoundary(mY+2, mX+1) || visited[mY+2][mX+1]) return false;
    	return true;
    }
    
    private static boolean chkCanGoWest(int mY, int mX) {
    	// 5곳 확인
    	if (!chkBoundary(mY-1, mX-1) || visited[mY-1][mX-1]) return false;
    	if (!chkBoundary(mY, mX-2) || visited[mY][mX-2]) return false;
    	if (!chkBoundary(mY+1, mX-2) || visited[mY+1][mX-2]) return false;
    	if (!chkBoundary(mY+1, mX-1) || visited[mY+1][mX-1]) return false;
    	if (!chkBoundary(mY+2, mX-1) || visited[mY+2][mX-1]) return false;
    	return true;
    }
    
    private static void writeVisited(int mY, int mX) {
    	visited[mY][mX] = true;
    	
    	for (int k = 0; k < 4; k++) {
    		int nY = mY + dy[k];
    		int nX = mX + dx[k];
    		visited[nY][nX] = true;
    	}
    }
    
    private static void goDown(int fidx) {
    	Stone now = stones.get(fidx);
    	
    	// 처음 시작 좌표 (골렘의 아래 부분)
    	int startR = nowR + 1;
    	int startC = now.startC;
    	boolean moved = false;
    	while (chkCanGoDown(startR, startC)) {
    		startR++;
    		moved = true;
    	}
    	startR--;
    	
    	if (moved) nowR = startR;
    }
    
    private static boolean chkCanGoDown(int mY, int mX) {
    	// 3곳 체크
    	if (!chkBoundary(mY, mX-1) || visited[mY][mX-1]) return false;
    	if (!chkBoundary(mY, mX+1) || visited[mY][mX+1]) return false;
    	if (!chkBoundary(mY+1, mX) || visited[mY+1][mX]) return false;
    	return true;
    }
    
    private static boolean chkBoundary(int i, int j) {
    	return (0 <= i && i <= R+1 && 1 <= j && j <= C);
    }
}