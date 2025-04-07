import java.io.*;
import java.util.*;
public class Main {
	static class Warrior{
		int r;
		int c;
		boolean isStopped;
		boolean isAlived;
		Warrior(int r, int c, boolean isStopped, boolean isAlived) {
			this.r = r;
			this.c = c;
			this.isStopped = isStopped;
			this.isAlived = isAlived;
		}
	}
	// 상 하 좌 우 시야각
	static int[][] watch_dy = {{-1, -1, -1}, {1, 1, 1}, {-1, 0, 1}, {-1, 0, 1}};
	static int[][] watch_dx = {{-1, 0, 1}, {-1, 0, 1}, {-1, -1, -1}, {1, 1, 1}};
	static int[] dy = {-1, 1, 0, 0}; // 상, 하, 좌, 우
	static int[] dx = {0, 0, -1, 1};
	static int[][] map, watched;
	static List<Warrior> warriors;
	static int N, M, startR, startC, endR, endC, sumPath, sumAttack;
	public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        StringTokenizer st = new StringTokenizer(br.readLine());
        N = Integer.parseInt(st.nextToken()); // 마을의 크기
        M = Integer.parseInt(st.nextToken()); // 전사의 수
        
        st = new StringTokenizer(br.readLine());
        startR = Integer.parseInt(st.nextToken());
        startC = Integer.parseInt(st.nextToken());
        endR = Integer.parseInt(st.nextToken());
        endC = Integer.parseInt(st.nextToken());
        
        st = new StringTokenizer(br.readLine());
        warriors = new ArrayList<>();
        for (int i = 0; i < M; i++) {
        	int wi = Integer.parseInt(st.nextToken());
        	int wj = Integer.parseInt(st.nextToken());
        	warriors.add(new Warrior(wi, wj, false, true));        	
        }
        
        map = new int[N][N];
        for (int i = 0; i < N; i++) {
        	st = new StringTokenizer(br.readLine());
        	for (int j = 0; j < N; j++) {
        		map[i][j] = Integer.parseInt(st.nextToken());
        	}
        }
        
        // 메두사 최단 거리 루트 구하기
        boolean chk = true;
        List<int[]> route = findRoute();
        if (route == null) chk = false;
        if (!chk) bw.write(String.valueOf(-1));
        
        if (chk) {
        	for (int[] path : route) {
        		int nowY = path[0];
        		int nowX = path[1];
        		
        		// 1. 메두사 이동
        		startR = nowY;
        		startC = nowX;
        		
        		for (int i = 0; i < warriors.size(); i++) {
        			Warrior now = warriors.get(i);
        			if (!now.isAlived) continue;
        			if (now.r == startR && now.c == startC) now.isAlived = false;
        		}
        		
        		// 2. 메두사 시선
        		watched = new int[N][N];
        		int sumRock = chooseSight();
        		
        		// 3. 전사 이동 + 공격
        		sumPath = 0;
        		sumAttack = 0;
        		moveWarrior();
        		
        		bw.write(sumPath + " " + sumRock + " " + sumAttack);
        		bw.newLine();
        	}
        	bw.write(String.valueOf(0));
        }
        bw.flush();
        bw.close();
    }
	
	private static void moveWarrior() {
		for (int w = 0; w < warriors.size(); w++) {
			Warrior now = warriors.get(w);
			if (!now.isAlived) continue;
			if (now.isStopped) {
				now.isStopped = false;
				continue;
			}
			
			// 첫 번째 이동 (상하좌우)
			int distance = Math.abs(now.r - startR) + Math.abs(now.c - startC);
			for (int k = 0; k < 4; k++) {
				int nextY = now.r + dy[k];
				int nextX = now.c + dx[k];
				int nextDist = Math.abs(nextY - startR) + Math.abs(nextX - startC);
				
				if (chkBoundary(nextY, nextX, N) && watched[nextY][nextX] != 1) {
					if (distance > nextDist) {
						now.r = nextY;
						now.c = nextX;
						sumPath++;
						break;
					}
				}
			}
			
			if (now.r == startR && now.c == startC) {
				now.isAlived = false;
				sumAttack++;
				continue;
			}
			
			// 두 번째 이동 (좌우상하)
			distance = Math.abs(now.r - startR) + Math.abs(now.c - startC);
			for (int k = 0; k < 4; k++) {
				int nextY = now.r + dy[(k+2)%4];
				int nextX = now.c + dx[(k+2)%4];
				int nextDist = Math.abs(nextY - startR) + Math.abs(nextX - startC);
				
				if (chkBoundary(nextY, nextX, N) && watched[nextY][nextX] != 1) {
					if (distance > nextDist) {
						now.r = nextY;
						now.c = nextX;
						sumPath++;
						break;
					}
				}
			}
			
			if (now.r == startR && now.c == startC) {
				now.isAlived = false;
				sumAttack++;
				continue;
			}
		}
	}
	
	private static int chooseSight() {
		// 0. 전사의 맵 분포도 필요 (같은 자리에 여러 명 있을 수 있으니까)
		// 1. 상하좌우로 시야 -> 돌로 변한 전사 수 비교
		
		int[][] wMap = new int[N][N];
		for (Warrior w : warriors) {
			if (!w.isAlived) continue;
			wMap[w.r][w.c]++;
		}
		
		int rock_largest = 0;
		for (int s = 0; s < 4; s++) { // 시야별
			int[][] tmp_watched = new int[N][N];
			int[] wDy = watch_dy[s];
			int[] wDx = watch_dx[s];
			
			int tmp_rock = 0;
			
			// 1. 가운데 
			int si = startR + watch_dy[s][1];
			int sj = startC + watch_dx[s][1];
			while (chkBoundary(si, sj, N)) {
				tmp_watched[si][sj] = 1;
				
				if (wMap[si][sj] > 0) {
					tmp_rock += wMap[si][sj];
					break;
				}
				
				si += watch_dy[s][1];
				sj += watch_dx[s][1];
			}
			
			// 2. 첫 번째 방향 (직선으로 내려가기 -> 전사가 있으면 / 없으면 -> 다음으로 내려가기)
			si = startR + watch_dy[s][0];
			sj = startC + watch_dx[s][0];
			while (chkBoundary(si, sj, N)) {
				// 시작점에서 전사를 만났을 때
				if (wMap[si][sj] > 0) {
					tmp_watched[si][sj] = 1;
					tmp_rock += wMap[si][sj];
					makeBlind(si, sj, s, 0, tmp_watched);
					break;
				} else {
					
					tmp_watched[si][sj] = 1;
				
					// 직선으로 내려가기
					int ni = si + watch_dy[s][1];
					int nj = sj + watch_dx[s][1];
					while (chkBoundary(ni, nj, N)) {
						if (tmp_watched[ni][nj] == 2) break;
						else tmp_watched[ni][nj] = 1;
					
						// 전사를 만났을 때
						if (wMap[ni][nj] > 0) {
							tmp_rock += wMap[ni][nj];
							makeBlind(ni, nj, s, 0, tmp_watched);
						}
					
						ni += watch_dy[s][1];
						nj += watch_dx[s][1];
					}
				
					// 다음 si, sj 로 이동하기 (대각선)
					si += watch_dy[s][0];
					sj += watch_dx[s][0];
				}
			}
			
			// 3. 세 번째 방향
			si = startR + watch_dy[s][2];
			sj = startC + watch_dx[s][2];
			while (chkBoundary(si, sj, N)) {
				// 시작점에서 전사를 만났을 때
				if (wMap[si][sj] > 0) {
					tmp_watched[si][sj] = 1;
					tmp_rock += wMap[si][sj];
					makeBlind(si, sj, s, 2, tmp_watched);
					break;
				} else {
					
					tmp_watched[si][sj] = 1;
				
					// 직선으로 내려가기
					int ni = si + watch_dy[s][1];
					int nj = sj + watch_dx[s][1];
					while (chkBoundary(ni, nj, N)) {
						if (tmp_watched[ni][nj] == 2) break;
						else tmp_watched[ni][nj] = 1;
					
						// 전사를 만났을 때
						if (wMap[ni][nj] > 0) {
							tmp_rock += wMap[ni][nj];
							makeBlind(ni, nj, s, 2, tmp_watched);
						}
					
						ni += watch_dy[s][1];
						nj += watch_dx[s][1];
					}
				
					// 다음 si, sj 로 이동하기 (대각선)
					si += watch_dy[s][2];
					sj += watch_dx[s][2];
				}
			}
			
			// 비교
			if (rock_largest < tmp_rock) {
				rock_largest = tmp_rock;
				for (int r = 0; r < N; r++) {
					for (int c = 0; c < N; c++) {
						watched[r][c] = tmp_watched[r][c];
					}
				}
			}
		}
		
		// 네 방향 비교 끝났을 때 - 선택된 시야 & 전사 존재 -> stopped
		for (int r = 0; r < N; r++) {
			for (int c = 0; c < N; c++) {
				if (watched[r][c] == 1 && wMap[r][c] > 0) {
					for (Warrior w : warriors) {
						if (!w.isAlived) continue;
						if (w.r == r && w.c == c) w.isStopped = true;
					}
				}
			}
		}
		
		return rock_largest;
	}
	
	private static void makeBlind(int si, int sj, int s, int dir, int[][] tmp_watched) {
		// 대각선 dir 방향으로 내려감
		while (chkBoundary(si, sj, N)) {
			// 1. 직선으로 내려감 (2로 만들기)
			if (tmp_watched[si][sj] == 0) tmp_watched[si][sj] = 2;
			
			int ni = si + watch_dy[s][1];
			int nj = sj + watch_dx[s][1];
			while (chkBoundary(ni, nj, N)) {
				tmp_watched[ni][nj] = 2;
				ni += watch_dy[s][1];
				nj += watch_dx[s][1];
			}
			
			// 다음 대각선 시작점으로 이동
			si += watch_dy[s][dir];
			sj += watch_dx[s][dir];
		}
	}
	
	static class Node {
		int r;
		int c;
		List<int[]> path;
		Node(int r, int c, List<int[]> path) {
			this.r = r;
			this.c = c;
			this.path = path;
		}
	}
	
	private static List<int[]> findRoute() {
		Queue<Node> myqueue = new LinkedList<>();
		myqueue.add(new Node(startR, startC, new ArrayList<>()));
		
		boolean[][] visited = new boolean[N][N];
		visited[startR][startC] = true;
		
		while (!myqueue.isEmpty()) {
			Node now = myqueue.poll();
			int nowY = now.r;
			int nowX = now.c;
			List<int[]> nowPath = now.path;
			
			if (nowY == endR && nowX == endC) {
				nowPath.remove(nowPath.size()-1); // 도착지 삭제
				return nowPath;
			}
			
			for (int k = 0; k < 4; k++) {
				int nextY = nowY + dy[k];
				int nextX = nowX + dx[k];
				
				if (chkBoundary(nextY, nextX, N) && !visited[nextY][nextX]) {
					if (map[nextY][nextX] == 0) {
						visited[nextY][nextX] = true;
						
						List<int[]> newPath = new ArrayList<>(nowPath);
						newPath.add(new int[] {nextY, nextX});
						myqueue.add(new Node(nextY, nextX, newPath));
					}
				}
			}
		}
		
		return null;
	}
	
	private static boolean chkBoundary(int i, int j, int size) {
		return (0 <= i && i < size && 0 <= j && j < size);
	}
}
