import java.io.*;
import java.util.*;
public class Main {
	static class Knight {
		int r;
		int c;
		int height;
		int width;
		int blood;
		int damage;
		boolean isAlived;
		Knight(int r, int c, int height, int width, int blood, int damage, boolean isAlived) {
			this.r = r;
			this.c = c;
			this.height = height;
			this.width = width;
			this.blood = blood;
			this.damage = damage;
			this.isAlived = isAlived;
		}
	}
	static int[] dy = {-1, 0, 1, 0}; // 북 동 남 서
	static int[] dx = {0, 1, 0, -1};
	static int[][] map, kmap;
	static List<Knight> knights, damaged;
	static int L;
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
		StringTokenizer st = new StringTokenizer(br.readLine());
		L = Integer.parseInt(st.nextToken()); // LxL 체스판
		int N = Integer.parseInt(st.nextToken()); // N개의 줄에 걸친 기사들 정보
		int Q = Integer.parseInt(st.nextToken()); // 왕의 명령 수

		map = new int[L+1][L+1];
		for (int i = 1; i <= L ; i++) {
			st = new StringTokenizer(br.readLine());
			for (int j = 1; j <= L ; j++) {
				// 빈칸(0) 함정(1) 벽(2)
				map[i][j] = Integer.parseInt(st.nextToken());
			}
		}

		knights = new ArrayList<>();
		kmap = new int[L+1][L+1];
		for (int i = 0; i < N; i++) {
			st = new StringTokenizer(br.readLine());
			int r = Integer.parseInt(st.nextToken());
			int c = Integer.parseInt(st.nextToken());
			int h = Integer.parseInt(st.nextToken());
			int w = Integer.parseInt(st.nextToken());
			int k = Integer.parseInt(st.nextToken());
			knights.add(new Knight(r,c,h,w,k,0,true));

			// kmap 에 기사 자리 색칠하기 (실제 List의 idx + 1 로 기사 분별)
			for (int i2 = 0; i2 < h; i2++) {
				for (int j2 = 0; j2 < w; j2++) {
					kmap[r+i2][c+j2] = i+1;
				}
			}
		}
		
		for (int q = 0; q < Q; q++) {
			st = new StringTokenizer(br.readLine());
			int kidx = Integer.parseInt(st.nextToken()) - 1;
			int dir = Integer.parseInt(st.nextToken()); // 해당 방향으로 한 칸 이동

			Knight now = knights.get(kidx);
			if (!now.isAlived) continue;

			// 1. 명령 받은 기사 이동
			damaged = new ArrayList<>(); // 명령 받은 기사에 의해 대미지 입은 기사 리스트
			moveKnights(kidx, dir);
			
			//printMap(kmap);
			
			// 2. 대미지 계산
			calculateDamage();
		}

		// 생존한 기사들의 총 대미지 합
		int sum = 0;
		for (Knight k : knights) {
			if (!k.isAlived) continue;
			//System.out.println("살아남은 기사 idx : " + knights.indexOf(k));
			sum += k.damage;
		}

		bw.write(String.valueOf(sum));
		bw.flush();
		bw.close();
    }
    
    private static void calculateDamage() {
    	for (int k = 0; k < damaged.size(); k++) {
    		Knight now = damaged.get(k);
    		
    		int damagedCnt = 0;
    		for (int i2 = 0; i2 < now.height; i2++) {
				for (int j2 = 0; j2 < now.width; j2++) {
					// 해당 칸 좌표
					int chkY = now.r + i2;
					int chkX = now.c + j2;
					
					if (map[chkY][chkX] == 1) damagedCnt++;
				}
			}
    		
    		//System.out.println(knights.indexOf(now) + " 기사 idx 의 대미지 : " + damagedCnt);
    		
    		// 대미지 업데이트
    		now.damage += damagedCnt;
    		
    		// 대미지만큼 체력 감소
    		now.blood -= damagedCnt;
    		if (now.blood <= 0) {
    			//System.out.println("죽었다 idx : " + knights.indexOf(now));
    			now.isAlived = false;
    			
    			// kmap 에서 삭제
    			for (int i2 = 0; i2 < now.height; i2++) {
    				for (int j2 = 0; j2 < now.width; j2++) {
    					// 해당 칸 좌표
    					int chkY = now.r + i2;
    					int chkX = now.c + j2;
    					
    					kmap[chkY][chkX] = 0;
    				}
    			}
    		}
    	}
    }
    
    private static void moveKnights(int kidx, int dir) {
    	// 동시 이동 - tmpMap
    	int[][] tmpMap = new int[L+1][L+1];
    	HashSet<Knight> tmpDamaged = new HashSet<>();
    	List<int[]> moved = new ArrayList<>(); // 기사idx, 움직인r, 움직인c
    	
    	Queue<int[]> myqueue = new LinkedList<>();
    	myqueue.add(new int[] {kidx, dir});
    	
    	boolean[][] visited = new boolean[L+1][L+1];
    	visited[knights.get(kidx).r][knights.get(kidx).c] = true;
    	
    	// 건들여진 기사들을 모두 처리하기 (이동하려는 자리에 벽이나 다른 기사가 없을 때까지 진행)
    	boolean canGo = true;
    	while (!myqueue.isEmpty()) {
    		int[] q = myqueue.poll();
    		int nowIdx = q[0];
    		int nowDir = q[1];
    		//System.out.println("현재 이동하는 기사 : " + (nowIdx+1));
    		
    		// (r,c)
        	Knight now = knights.get(nowIdx);
        	moved.add(new int[] {nowIdx, knights.get(nowIdx).r + dy[nowDir], knights.get(nowIdx).c + dx[nowDir]});
        	
    		for (int i2 = 0; i2 < now.height; i2++) {
    			for (int j2 = 0; j2 < now.width; j2++) {
    				// 색칠하는 칸
    				//System.out.println("현재 위치 : " + (now.r+i2) + " " + (now.c+j2));
    				int nextY = now.r+i2+dy[nowDir];
    				int nextX = now.c+j2+dx[nowDir];
    				
    				//System.out.println("가려는 위치 : " + nextY + " " + nextX);
    				
    				// 영역 밖이면 갈 수 없음
    	        	if (!(1 <= nextY && nextY <= L && 1 <= nextX && nextX <= L)) {
    	        		//System.out.println("영역 밖이어서 갈 수 없음");
    	        		canGo = false;
    	        		break;
    	        	}
    				
    				// 벽이면 갈 수 없음
    				if (map[nextY][nextX] == 2) {
    					//System.out.println("벽이어서 갈 수 없음");
    					canGo = false;
    					break;
    				}
    				
    				// 내가 아닌 다른 기사 건들였을 때
    				if (kmap[nextY][nextX] > 0 && kmap[nextY][nextX] != nowIdx+1) {
    					// 건들인 기사 리스트 인덱스
    					int attacked = kmap[nextY][nextX] - 1;
    					myqueue.add(new int[] {attacked, nowDir});
    					tmpDamaged.add(knights.get(attacked));
    					//System.out.println("건들여진 기사 idx : " + (attacked));
    				}
    				
    				// 임시 맵에 해당 칸 기록
    				tmpMap[nextY][nextX] = nowIdx+1;
    			}
    			if (!canGo) break;
    		}
    		if (!canGo) break;
    	}
    	
    	// 이동가능하면 맵 복사
    	if (canGo) {
    		for (int i = 1; i <= L; i++) {
    			for (int j = 1; j <= L; j++) {
    				kmap[i][j] = tmpMap[i][j];
    			}
    		}
    		
    		// 건들이지 않은 기사도 바뀐 맵에 추가해야 함 - tmpMap에는 없음
    		List<Knight> tmp = new ArrayList<>();
    		for (int i = 0; i < knights.size(); i++) {
    			if (!knights.get(i).isAlived) continue;
    			tmp.add(knights.get(i));
    		}
    		for (int i = 0; i < moved.size(); i++) {
    			//System.out.println("움직인 기사: " + moved.get(i)[0]);
    			tmp.remove(knights.get(moved.get(i)[0]));
    		}
    		for (int i = 0; i < tmp.size(); i++) {
    			Knight now = tmp.get(i); // tmp 의 인덱스는 실제 기사의 인덱스가 아님!!!
    			int realIdx = knights.indexOf(now);
    			//System.out.println("움직이지 않았지만 맵에 추가한 기사: " + realIdx);
    			
    			for (int i2 = 0; i2 < now.height; i2++) {
    				for (int j2 = 0; j2 < now.width; j2++) {
    					kmap[now.r+i2][now.c+j2] = realIdx+1;
    				}
    			}
    		}
    		
    		// 피해 받은 기사 리스트 복사
    		for (Knight k : tmpDamaged) {
    			if (knights.indexOf(k) == kidx) continue;
    			damaged.add(k);
    		}
    		
    		// 움직인 기사들 위치 업데이트
    		for (int[] m : moved) {
    			Knight k = knights.get(m[0]);
    			k.r = m[1];
    			k.c = m[2];
    		}
    	}
    }

//	private static void printMap(int[][] map) {
//		for (int i = 1; i <= L; i++) {
//			for (int j = 1; j <= L; j++) {
//				System.out.print(map[i][j] + " ");
//			}
//			System.out.println();
//		}
//		System.out.println();
//	}
}