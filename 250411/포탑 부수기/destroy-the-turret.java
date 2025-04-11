import java.util.*;
import java.io.*;
public class Main {
	// 우 하 좌 상
	static int[] dy = {-1, 0, 1, 1, 1, 0, -1, -1};
	static int[] dx = {1, 1, 1, 0, -1, -1, -1, 0};
	static int N, M, tIdx;
	static int[][] map, tMap;
	static boolean[][] visited;
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        StringTokenizer st = new StringTokenizer(br.readLine());
        N = Integer.parseInt(st.nextToken());
        M = Integer.parseInt(st.nextToken());
        int K = Integer.parseInt(st.nextToken());
        
        map = new int[N+1][M+1];
        tMap = new int[N+1][M+1]; // 공격한지 가장 오래된 포탑 선정 시 사용
        tIdx = 1;
        for (int i = 1; i <= N; i++) {
        	st = new StringTokenizer(br.readLine());
        	for (int j = 1; j <= M; j++) {
        		map[i][j] = Integer.parseInt(st.nextToken());
        		
        		if (map[i][j] == 0) continue;
        		else {
        			tMap[i][j] = tIdx; // 모두 같은 우선순위 배정 (공격한지 가장 오래된 포탑 선정 시 사용)
        		}
        	}
        }
        
        tIdx++;
        
        for (int turn = 0; turn < K; turn++) {
        	
//        	for (int r = 1; r <= N; r++) {
//        		for (int c = 1; c <= M; c++) {
//        			System.out.print(map[r][c] + " ");
//        		}
//        		System.out.println();
//        	}
//        	System.out.println();
        	
        	// 0. 4번 포탑 정비를 위해 미리 공격했는지, 피해 입었는지 알 수 있는 방문 배열 생성
        	visited = new boolean[N+1][M+1];
        	
        	// 1. 공격자 선정
        	int[] attackerPos = chooseAttacker();
        	int attackerY = attackerPos[0];
        	int attackerX = attackerPos[1];
        	visited[attackerY][attackerX] = true;
        	
//        	System.out.println("공격자 좌표 : " + attackerY + " " + attackerX);
        	
        	// 핸디캡 적용 N+M 공격력 증가
        	map[attackerY][attackerX] += N+M;
        	
        	// 공격자 우선 순위 맵에 반영
        	tMap[attackerY][attackerX] = tIdx++; // 높을수록 최근에 공격함
        	
        	// 2. 가장 강한 포탑 선정
        	int[] targetPos = chooseTarget(attackerY, attackerX);
        	int targetY = targetPos[0];
        	int targetX = targetPos[1];
        	visited[targetY][targetX] = true;
        	
//        	System.out.println("타겟 좌표 : " + targetY + " " + targetX);
        	
        	// 3. 공격 - visited 같이 처리해주기
        	boolean chkAttack = attackWithLaser(attackerY, attackerX, targetY, targetX);
        	if (!chkAttack) attackWithBomb(attackerY, attackerX, targetY, targetX);
        	
        	// 4. 포탑 정비
        	for (int i = 1; i <= N; i++) {
        		for (int j = 1; j <= M; j++) {
        			if (map[i][j] == 0) visited[i][j] = true; // 0 처리
        		}
        	}
        	
        	// 공격과 무관했던 포탑 공격력 1 증가
        	for (int i = 1; i <= N; i++) {
        		for (int j = 1; j <= M; j++) {
        			if (!visited[i][j]) map[i][j]++;
        		}
        	}
        	
        	// 5. 부서지지 않은 포탑이 1개가 된다면 즉시 종료
        	int cnt = 0;
        	for (int i = 1; i <= N; i++) {
        		for (int j = 1; j <= M; j++) {
        			if (map[i][j] != 0) cnt++;
        		}
        	}
        	
        	if (cnt == 1) break;
        }
        
        // 남아있는 포탑 중 가장 강한 포탑의 공격력을 출력
        int ans = 0;
        for (int i = 1; i <= N; i++) {
        	for (int j = 1; j <= M; j++) {
        		if (map[i][j] == 0) continue;
        		ans = Math.max(ans, map[i][j]);
        	}
        }
        
        bw.write(String.valueOf(ans));
        bw.flush();
        bw.close();
    }
    
    private static void attackWithBomb(int attackerY, int attackerX, int targetY, int targetX) {
//    	System.out.println("포탄으로 공격");
    	// 1. 공격 대상에 피해 입히기 - 공격자의 공격력 만큼
    	int damage = map[attackerY][attackerX];
    	map[targetY][targetX] -= damage;
    	if (map[targetY][targetX] < 0) map[targetY][targetX] = 0;
    	
    	// 2. 주위 8개 포탑 피해 입히기 (공격자 제외) - 공격자의 공격력/2
    	// visited 에 표시하기
    	damage = map[attackerY][attackerX] / 2;
    	for (int k = 0; k < 8; k++) {
    		int nextY = targetY + dy[k];
    		int nextX = targetX + dx[k];
    		
    		// 이어지는 맵
			if (nextY == 0) nextY = N;
			else if (nextY == N+1) nextY = 1;
			if (nextX == 0) nextX = M;
			else if (nextX == M+1) nextX = 1;
			
			// 공격자 제외
			if (nextY == attackerY && nextX == attackerX) continue;
			
			// 0 제외
			if (map[nextY][nextX] == 0) continue;
			
			map[nextY][nextX] -= damage;
			visited[nextY][nextX] = true;
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
    
    // 3. 레이저 공격
    private static boolean attackWithLaser(int attackerY, int attackerX, int targetY, int targetX) {
    	// 최단거리 이동 - 우하좌상 탐색, 부서진 포탑 X, 맵이 이어짐
    	// 1. 최단 거리 구하기
    	Queue<Node> myqueue = new LinkedList<>();
    	myqueue.add(new Node(attackerY, attackerX, new ArrayList<>()));
    	
    	boolean[][] v = new boolean[N+1][M+1];
    	v[attackerY][attackerX] = true;
    	
    	List<int[]> shortestPath = new ArrayList<>();
    	while (!myqueue.isEmpty()) {
    		Node now = myqueue.poll();
    		int nowY = now.r;
    		int nowX = now.c;
    		List<int[]> nowPath = now.path;
    		
    		if (nowY == targetY && nowX == targetX) { // 도착
    			shortestPath = nowPath;
    			break;
    		}
    		
    		for (int k = 1; k < 8; k += 2) {
    			int nextY = nowY + dy[k];
    			int nextX = nowX + dx[k];
    			
    			// 이어지는 맵
    			if (nextY == 0) nextY = N;
    			else if (nextY == N+1) nextY = 1;
    			if (nextX == 0) nextX = M;
    			else if (nextX == M+1) nextX = 1;
    			    			
    			if (!v[nextY][nextX]) { // 방문하지 않은 곳
    				if (map[nextY][nextX] != 0) { // 0 이 아닌 곳
    					v[nextY][nextX] = true;
    					List<int[]> newPath = new ArrayList<>(nowPath);
    					newPath.add(new int[] {nextY, nextX});
    					myqueue.add(new Node(nextY, nextX, newPath));
    					//System.out.println("레이저 이동 : " + nextY + " " + nextX);
    				}
    			}
    		}
    	}
    	
    	// 도달 불가능
    	if (shortestPath.size() == 0) return false;
    	
    	// 마지막 도착지는 제외해주기
    	shortestPath.remove(shortestPath.size()-1);
    	
    	// 2. 공격 대상에 피해 입히기 - 공격자의 공격력 만큼
    	int damage = map[attackerY][attackerX];
    	map[targetY][targetX] -= damage;
    	if (map[targetY][targetX] < 0) map[targetY][targetX] = 0;
    	
    	// 3. 레이저 경로 포탑에 피해 입히기 - 공격자의 공격력/2
    	// visited 에 표시하기
    	damage = map[attackerY][attackerX] / 2;
    	for (int i = 0; i < shortestPath.size(); i++) {
    		int[] now = shortestPath.get(i);
    		int laserY = now[0];
    		int laserX = now[1];
//    		System.out.println("최단거리 : " + laserY + " " + laserX);
    		
    		map[laserY][laserX] -= damage;
    		if (map[laserY][laserX] < 0) map[laserY][laserX] = 0;
    		
    		visited[laserY][laserX] = true;
    	}
    	
    	return true;
    }
    
    // 2. 타겟 선정 - 공격력이 가장 높은 포탑 구하기
    private static int[] chooseTarget(int attackerY, int attackerX) {
//    	System.out.println("타겟 선정");
    	// 우선순위에서 빠지면 0으로 만들어줌 (다음 조건에 적용되지 않게)
    	int[][] tmpMap = new int[N+1][M+1]; 
    	for (int i = 1; i <= N; i++) {
    		for (int j = 1; j <= M; j++) {
    			tmpMap[i][j] = map[i][j];
    		}
    	}
    	
    	// 공격자 제외
    	tmpMap[attackerY][attackerX] = 0;
    	
    	// 우선순위
    	// 1. 공격력이 가장 높은 포탑
    	int strongestVal = 0;
    	for (int i = 1; i <= N; i++) {
    		for (int j = 1; j <= M; j++) {
    			if (tmpMap[i][j] == 0) continue;
    			strongestVal = Math.max(strongestVal, tmpMap[i][j]);
    		}
    	}
    	
    	int strongestCnt = 0;
    	for (int i = 1; i <= N; i++) {
    		for (int j = 1; j <= M; j++) {
    			if (tmpMap[i][j] == 0) continue;
    			if (tmpMap[i][j] == strongestVal) strongestCnt++;
    			else tmpMap[i][j] = 0;
    		}
    	}
    	
    	int biggestY = 0;
    	int biggestX = 0;
    	if (strongestCnt == 1) {
    		for (int i = 1; i <= N; i++) {
    			for (int j = 1; j <= M; j++) {
    				if (tmpMap[i][j] == 0) continue;
    				if (tmpMap[i][j] == strongestVal) {
    					biggestY = i;
    					biggestX = j;
//    					System.out.println("공격력이 가장 높음");
    					return new int[] {biggestY, biggestX};
    				}
    			}
    		}
    	}
    	
    	// 2. 공격한지 가장 오래된 포탑 - tMap 에서 수가 가장 낮음 (1부터 시작)
    	int oldestVal = Integer.MAX_VALUE;
    	for (int i = 1; i <= N; i++) {
    		for (int j = 1; j <= M; j++) {
    			if (tmpMap[i][j] == 0) continue;
    			oldestVal = Math.min(oldestVal, tMap[i][j]);
    		}
    	}
    	
    	int oldestCnt = 0;
    	for (int i = 1; i <= N; i++) {
    		for (int j = 1; j <= M; j++) {
    			if (tmpMap[i][j] == 0) continue;
    			if (oldestVal == tMap[i][j]) oldestCnt++;
    			else tmpMap[i][j] = 0;
    		}
    	}
    	
    	if (oldestCnt == 1) { // 공격한지 가장 오래된 포탑이 1개라면 좌표값 반환
    		for (int i = 1; i <= N; i++) {
    			for (int j = 1; j <= M; j++) {
    				if (tmpMap[i][j] == 0) continue;
    				if (tMap[i][j] == oldestVal) {
    					biggestY = i;
    					biggestX = j;
//    					System.out.println("공격한지 가장 오래됨");
    					return new int[] {biggestY, biggestX};
    				}
    			}
    		}
    	}
    	
    	// 3. 행+열 합이 가장 작은
    	int smallestSum = Integer.MAX_VALUE;
    	for (int i = 1; i <= N; i++) {
    		for (int j = 1; j <= M; j++) {
    			if (tmpMap[i][j] == 0) continue;
    			
    			smallestSum = Math.min(smallestSum, i+j);
    		}
    	}
    	
    	int smallestCnt = 0;
    	for (int i = 1; i <= N; i++) {
    		for (int j = 1; j <= M; j++) {
    			if (tmpMap[i][j] == 0) continue;
    			if ((i+j) == smallestSum) smallestCnt++;
    			else tmpMap[i][j] = 0;
    		}
    	}
    	
    	if (smallestCnt == 1) { // 행과 열의 합이 가장 작은 포탑이 1개라면 좌표값 반환
    		for (int i = 1; i <= N; i++) {
    			for (int j = 1; j <= M; j++) {
    				if (tmpMap[i][j] == 0) continue;
    				if ((i+j) == smallestSum) {
    					biggestY = i;
    					biggestX = j;
//    					System.out.println("행과 열의 합이 가장 작음");
    					return new int[] {biggestY, biggestX};
    				}
    			}
    		}
    	}
    	
    	// 4. 열 값이 가장 작은
    	boolean chk = false;
    	for (int j = 1; j <= M; j++) {
    		for (int i = 1; i <= N; i++) {
    			if (tmpMap[i][j] != 0) {
    				biggestY = i;
    				biggestX = j;
    				chk = true;
    				break;
    			}
    		}
    		if (chk) break;
    	}
    	
//    	System.out.println("열이 가장 작음");
    	return new int[] {biggestY, biggestX};
    }
    
    // 1. 공격자 선정 - 공격력이 가장 낮은 포탑 구하기
    private static int[] chooseAttacker() {
    	// 우선순위에서 빠지면 0으로 만들어줌 (다음 조건에 적용되지 않게)
    	int[][] tmpMap = new int[N+1][M+1]; 
    	for (int i = 1; i <= N; i++) {
    		for (int j = 1; j <= M; j++) {
    			tmpMap[i][j] = map[i][j];
    		}
    	}
    	
    	// 우선순위
    	// 1. 공격력이 가장 낮은 포탑
    	int smallestVal = Integer.MAX_VALUE;
    	for (int i = 1; i <= N; i++) {
    		for (int j = 1; j <= M; j++) {
    			if (tmpMap[i][j] == 0) continue;
    			smallestVal = Math.min(smallestVal, tmpMap[i][j]);
    		}
    	}
    	
    	int smallestCnt = 0;
    	for (int i = 1; i <= N; i++) {
    		for (int j = 1; j <= M; j++) {
    			if (tmpMap[i][j] == 0) continue;
    			if (tmpMap[i][j] == smallestVal) smallestCnt++;
    			else tmpMap[i][j] = 0;
    		}
    	}
    	
    	int smallestY = 0;
		int smallestX = 0;
    	if (smallestCnt == 1) { // 공격력이 가장 낮은 포탑이 1개라면 좌표값 반환
    		for (int i = 1; i <= N; i++) {
    			for (int j = 1; j <= M; j++) {
    				if (tmpMap[i][j] == 0) continue;
    				if (tmpMap[i][j] == smallestVal) {
    					smallestY = i;
    					smallestX = j;
    					return new int[] {smallestY, smallestX};
    				}
    			}
    		}
    	}
    	
    	// 2. 가장 최근에 공격한 포탑
    	// 가장 최근에 공격한 포탑 == tMap 에서 수가 가장 높음
    	int latestVal = 0;
    	for (int i = 1; i <= N; i++) {
    		for (int j = 1; j <= M; j++) {
    			if (tmpMap[i][j] == 0) continue;
    			latestVal = Math.max(latestVal, tMap[i][j]);
    		}
    	}
    	
    	int latestCnt = 0;
    	for (int i = 1; i <= N; i++) {
    		for (int j = 1; j <= M; j++) {
    			if (tmpMap[i][j] == 0) continue;
    			if (latestVal == tMap[i][j]) latestCnt++;
    			else tmpMap[i][j] = 0;
    		}
    	}
    	
    	if (latestCnt == 1) { // 가장 최근에 공격한 포탑이 1개라면 좌표값 반환
    		for (int i = 1; i <= N; i++) {
    			for (int j = 1; j <= M; j++) {
    				if (tmpMap[i][j] == 0) continue;
    				if (tMap[i][j] == latestVal) {
    					smallestY = i;
    					smallestX = j;
    					return new int[] {smallestY, smallestX};
    				}
    			}
    		}
    	}
    	
    	// 3. 행+열 합이 가장 큰
    	int biggestSum = 0;
    	for (int i = 1; i <= N; i++) {
    		for (int j = 1; j <= M; j++) {
    			if (tmpMap[i][j] == 0) continue;
    			biggestSum = Math.max(biggestSum, i+j);
    		}
    	}
    	
    	int biggestCnt = 0;
    	for (int i = 1; i <= N; i++) {
    		for (int j = 1; j <= M; j++) {
    			if (tmpMap[i][j] == 0) continue;
    			if ((i+j) == biggestSum) biggestCnt++;
    			else tmpMap[i][j] = 0;
    		}
    	}
    	
    	if (biggestCnt == 1) { // 행과 열의 합이 가장 큰 포탑이 1개라면 좌표값 반환
    		for (int i = 1; i <= N; i++) {
    			for (int j = 1; j <= M; j++) {
    				if (tmpMap[i][j] == 0) continue;
    				if ((i+j) == biggestSum) {
    					smallestY = i;
    					smallestX = j;
    					return new int[] {smallestY, smallestX};
    				}
    			}
    		}
    	}
    	
    	// 4. 열 값이 가장 큰
    	boolean chk = false;
    	for (int j = M; j >= 1; j--) {
    		for (int i = 1; i <= N; i++) {
    			if (tmpMap[i][j] != 0) {
    				smallestY = i;
    				smallestX = j;
    				chk = true;
    				break;
    			}
    		}
    		if (chk) break;
    	}

    	return new int[] {smallestY, smallestX};
    }
}