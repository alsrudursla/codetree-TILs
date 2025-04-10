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
    static int[][] visited;
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
        map = new int[R+3][C+1];
        visited = new int[R+3][C+1];
        int ans = 0;
        for (int t = 1; t <= K; t++) {
        	//System.out.println("-----------------------" + " " + t + " ---------------------------");
        	// 처음 시작 좌표
        	nowR = 1;
            nowC = stones.get(t-1).startC;

            // 이동 루프
            boolean canGo = true;
            while (canGo) {
                // 1. 아래로 이동
            	goDown(t-1);
            	//System.out.println("아래로 이동 후 : " + nowR + " " + nowC);
            	if (nowR == R+1) { // 맵의 끝에 도달 (맵의 끝은 R+2)
            		writeVisited(nowR, nowC, t);
            		break;
            	}
            	
            	// 2. 서쪽으로 이동
            	// 서쪽으로 이동했으면 다시 아래부터 시작
            	// 서쪽으로 이동 못했으면 동쪽으로 이동
            	if (chkCanGoWest(nowR, nowC)) {
            		// 좌표 이동 + 출구 반시계 회전
            		nowR++;
            		nowC--;
            		int ori_dir = stones.get(t-1).exitDir;
            		stones.get(t-1).exitDir = ((ori_dir+3)%4);
            		//System.out.println("서쪽으로 이동 후 : " + nowR + " " + nowC + " 출구 " + stones.get(t-1).exitDir);
            		continue; // 다시 반복 : 아래 -> 서 -> 동
            	} else {
            		if (chkCanGoEast(nowR, nowC)) {
            			// 좌표 이동 + 출구 시계 회전
            			nowR++;
            			nowC++;
            			int ori_dir = stones.get(t-1).exitDir;
                		stones.get(t-1).exitDir = ((ori_dir+1)%4);
            			//System.out.println("동쪽으로 이동 후 : " + nowR + " " + nowC + " 출구 " + stones.get(t-1).exitDir);
            			continue;
            		} else {
            			canGo = false;
            		}
            	}
            }

            // 최종 좌표가 맵 안에 없으면 맵 초기화 -> 맵 합산 없이 다음 정령으로
            if (!chkInTheMap(nowR, nowC)) {
                for (int i = 0; i <= R+2; i++) {
                    for (int j = 0; j <= C; j++) {
                        visited[i][j] = 0;
                    }
                }
                continue;
            } else {
            	// 맵 업데이트
                writeVisited(nowR, nowC, t);
            }
            
//            for (int r = 3; r <= R+2; r++) {
//        		for (int c = 1; c <= C; c++) {
//        			System.out.print(visited[r][c] + " ");
//        		}
//        		System.out.println();
//        	}
//            System.out.println();

            // 최종 행 합산
            if (nowR == R+1) { // 맵의 끝은 R+2
                ans += R; 
                //System.out.println(ans);
            } else {
                ans += bfs(nowR, nowC, stones.get(t-1).exitDir, t) - 2;
                //System.out.println(ans);
            }
        }

        bw.write(String.valueOf(ans));
        bw.flush();
        bw.close();
    }
    
    private static int bfs(int mY, int mX, int exitDir, int fidx) {
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
    		
    		if (chkBoundary(chkY, chkX) && visited[chkY][chkX] != fidx && visited[chkY][chkX] != 0) {
    			// 범위 내 && 다른 골렘이 있음 -> 행 이동 가능
    			canGo = true;
    			break;
    		}
    	}
    	
    	//System.out.println(canGo);
    	
    	if (!canGo) return mY + 1;
    	
    	Queue<int[]> myqueue = new LinkedList<>();
    	myqueue.add(new int[] {exitY, exitX, -fidx}); // i, j, 현재 칸에 쓰인 번호
    	
    	boolean[][] v = new boolean[R+3][C+1];
    	v[exitY][exitX] = true;
    	
    	int biggestY = mY + 1; // 현재 골렘에서 갈 수 있는 가장 큰 행 좌표
    	while (!myqueue.isEmpty()) {
    		int[] now = myqueue.poll();
    		int nowY = now[0];
    		int nowX = now[1];
    		int nowNum = now[2];
    		
    		biggestY = Math.max(biggestY, nowY);
    		
    		for (int k = 0; k < 4; k++) {
    			int nextY = nowY + dy[k];
    			int nextX = nowX + dx[k];
    			
    			// 범위 내 && 가지 않았던 곳 && 0이면 안됨
    			if (chkBoundary(nextY, nextX) && !v[nextY][nextX] && visited[nextY][nextX] != 0) {
    				// 내 골렘의 칸 혹은 내 골렘의 출구, 내 골렘의 출구에선 다른 번호 칸 이동 가능
    				if (visited[nextY][nextX] == nowNum || visited[nextY][nextX] == -nowNum || nowNum < 0) {
        				v[nextY][nextX] = true;
        				myqueue.add(new int[] {nextY, nextX, visited[nextY][nextX]});
        				//System.out.println(nextY + " " + nextX);
    				}
    			}
    		}
    	}
    	
    	return biggestY;
    }
    
    private static boolean chkInTheMap(int mY, int mX) {
    	if (!(3 <= mY && mY <= R+2 && 1 <= mX && mX <= C)) return false;
    	for (int k = 0; k < 4; k++) {
    		int chkY = mY + dy[k];
    		int chkX = mX + dx[k];
    		if (!(3 <= chkY && chkY <= R+2 && 1 <= chkX && chkX <= C)) return false;
    	}
    	return true;
    }
    
    private static boolean chkCanGoEast(int mY, int mX) {
    	// 5곳 확인
    	if (!chkBoundary(mY-1, mX+1) || visited[mY-1][mX+1] != 0) return false;
    	if (!chkBoundary(mY, mX+2) || visited[mY][mX+2] != 0) return false;
    	if (!chkBoundary(mY+1, mX+2) || visited[mY+1][mX+2] != 0) return false;
    	if (!chkBoundary(mY+1, mX+1) || visited[mY+1][mX+1] != 0) return false;
    	if (!chkBoundary(mY+2, mX+1) || visited[mY+2][mX+1] != 0) return false;
    	return true;
    }
    
    private static boolean chkCanGoWest(int mY, int mX) {
    	// 5곳 확인
    	if (!chkBoundary(mY-1, mX-1) || visited[mY-1][mX-1] != 0) return false;
    	if (!chkBoundary(mY, mX-2) || visited[mY][mX-2] != 0) return false;
    	if (!chkBoundary(mY+1, mX-2) || visited[mY+1][mX-2] != 0) return false;
    	if (!chkBoundary(mY+1, mX-1) || visited[mY+1][mX-1] != 0) return false;
    	if (!chkBoundary(mY+2, mX-1) || visited[mY+2][mX-1] != 0) return false;
    	return true;
    }
    
    private static void writeVisited(int mY, int mX, int fidx) {
    	visited[mY][mX] = fidx;
    	
    	int exitAreaY = mY + dy[stones.get(fidx-1).exitDir];
    	int exitAreaX = mX + dx[stones.get(fidx-1).exitDir];
    	for (int k = 0; k < 4; k++) {
    		int nY = mY + dy[k];
    		int nX = mX + dx[k];
    		
    		if (nY == exitAreaY && nX == exitAreaX) visited[nY][nX] = -fidx;
    		else visited[nY][nX] = fidx;
    	}
    }
    
    private static void goDown(int fidx) {
    	Stone now = stones.get(fidx);
    	
    	// 처음 시작 좌표 (골렘의 아래 부분)
    	int startR = nowR + 1;
    	int startC = nowC;
    	//System.out.println("아래 이동 시작 좌표 : " + startR + " " + startC);
    	boolean moved = false;
    	while (chkCanGoDown(startR, startC)) {
    		startR++;
    		moved = true;
    		//System.out.println("아래 이동 중 : " + startR + " " + startC);
    	}
    	startR--;
    	
    	if (moved) nowR = startR;
    }
    
    private static boolean chkCanGoDown(int mY, int mX) {
    	// 3곳 체크
    	if (!chkBoundary(mY, mX-1) || visited[mY][mX-1] != 0) return false;
    	if (!chkBoundary(mY, mX+1) || visited[mY][mX+1] != 0) return false;
    	if (!chkBoundary(mY+1, mX) || visited[mY+1][mX] != 0) return false;
    	return true;
    }
    
    private static boolean chkBoundary(int i, int j) {
    	return (0 <= i && i <= R+2 && 1 <= j && j <= C);
    }
}