import java.util.*;
import java.io.*;
public class Main {
	static class Member {
		int r;
		int c;
		int distance;
		boolean isFinished;
		Member(int r, int c, int distance, boolean isFinished) {
			this.r = r;
			this.c = c;
			this.distance = distance;
			this.isFinished = isFinished;
		}
	}
	static int[] dy = {-1, 1, 0, 0}; // 상하좌우
	static int[] dx = {0, 0, -1, 1};
	static int N, M, endR, endC;
	static int[][] map;
	static List<Member> members;
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        StringTokenizer st = new StringTokenizer(br.readLine());
        N = Integer.parseInt(st.nextToken());
        M = Integer.parseInt(st.nextToken()); // 참가자 수
        int K = Integer.parseInt(st.nextToken()); // K 초 동안 반복
        
        map = new int[N+1][N+1];
        for (int i = 1; i <= N; i++) {
        	st = new StringTokenizer(br.readLine());
        	for (int j = 1; j <= N; j++) {
        		map[i][j] = Integer.parseInt(st.nextToken());
        	}
        }
        
        members = new ArrayList<>();
        for (int i = 0; i < M; i++) {
        	st = new StringTokenizer(br.readLine());
        	int r = Integer.parseInt(st.nextToken());
        	int c = Integer.parseInt(st.nextToken());
        	members.add(new Member(r,c,0,false));
        	map[r][c] += -(i+1); // 맵에 음수 값으로 기록 (멤버 인덱스 1부터 시작), 여러 명이 있을 수 있으니까 += 로 더해주기
        }
        
        st = new StringTokenizer(br.readLine());
        endR = Integer.parseInt(st.nextToken());
        endC = Integer.parseInt(st.nextToken());
        map[endR][endC] = -100; // 출구는 -100 으로 기록
        
        for (int turn = 1; turn <= K; turn++) {
        	//System.out.println("--------------------------------------------");
        	//printMap(map);
        	
        	// 1. 참가자 이동
        	moveMember();
        	
        	// 즉시 종료 조건 : 모든 참가자가 탈출 성공
        	boolean allFinished = true;
        	for (Member m : members) {
        		if (!m.isFinished) allFinished = false;
        	}
        	
        	if (allFinished) break;
        	
        	// 2. 정사각형 선택 좌상단 (r,c) 
        	int[] squarePos = chooseSquare();
        	int squareY = squarePos[0];
        	int squareX = squarePos[1];
        	int squareSize = squarePos[2];
        	//System.out.println("정사각형 좌상단 좌표 : " + squareY + " " + squareX + " size : " + squareSize);
        	
        	// 3. 미로 회전
        	rotateMap(squareY, squareX, squareSize);
        }
        
        int distanceSum = 0;
        for (Member m : members) distanceSum += m.distance;
        
        bw.write(String.valueOf(distanceSum));;
        bw.newLine();
        bw.write(endR + " " + endC);
        bw.flush();
        bw.close();
    }
    
    private static void rotateMap(int sY, int sX, int size) {
    	// 1. 정사각형 시계 방향 90도 회전 - 맵 회전, 사람 회전, 출구 회전
    	// 2. 회전된 벽 내구도 감소
    	
    	// 내구도 감소 + 원래 맵 떼어오기
    	int[][] ori_map = new int[size][size];
    	for (int i = sY; i <= sY+size-1; i++) {
    		for (int j = sX; j <= sX+size-1; j++) {
    			ori_map[i-sY][j-sX] = map[i][j];
    			
    			// 벽일 때 내구도 감소
    			if (1 <= ori_map[i-sY][j-sX] && ori_map[i-sY][j-sX] <= 9) {
    				ori_map[i-sY][j-sX]--;
    			}
    		}
    	}
    	
    	// 회전
    	int[][] tmp_map = new int[size][size];
    	for (int i = 0; i < ori_map.length; i++) {
    		for (int j = 0; j < ori_map[0].length; j++) {
    			tmp_map[j][size-1-i] = ori_map[i][j];
    		}
    	}
    	
    	
    	// 원래 맵에 붙여 넣기
    	for (int i = sY; i <= sY+size-1; i++) {
    		for (int j = sX; j <= sX+size-1; j++) {
    			map[i][j] = tmp_map[i-sY][j-sX];
    		}
    	}
    	
    	//printMap(map);
    	
    	// 참가자 좌표 갱신 - 원래 있었던 좌표가 회전하는 좌표 내에 있었으면.. 기준점 기준 회전 뭔지 알지
    	// 기본 회전 : tmp[j][size-1-i] = ori[i][j]
    	// 기준점 회전 : tmp[si + j][sj + size-1-i] = ori[i][j]
    	for (Member m : members) {
    		if (sY <= m.r && m.r <= sY+size-1 && sX <= m.c && m.c <= sX+size-1) {
    			// 상대좌표 회전
    			int tmpR = m.r - sY;
    			int tmpC = m.c - sX;
    			m.r = sY + tmpC;
    			m.c = sX + (size - 1 - tmpR);
    		}
    	}
    	
    	// 출구 좌표 갱신
    	int tmpR = endR - sY;
		int tmpC = endC - sX;
		endR = sY + tmpC;
		endC = sX + (size - 1 - tmpR);
    }
    
    private static int[] chooseSquare() {
    	// 1. 한 명 이상의 참가자와 출구를 포함한 가장 작은 정사각형 잡기 (우선 순위 r 작은 것 -> c 작은 것)
    	
    	// (1,1)~(N,M) 까지 사이즈 키워가면서 해보기
    	// 참가자 1명 충족, 출구 충족
    	int maxSquareSize = N;
    	for (int size = 2; size <= maxSquareSize; size++) {
    		for (int r = 1; r <= N-size+1; r++) { // 정사각형 사이즈 고려해서 갈 수 있는 최대
        		for (int c = 1; c <= N-size+1; c++) {
        			boolean memberExist = false;
        	    	boolean exitExist = false;
        			// 정사각형 범위 세로 : c + size -1 / 가로 : r + size - 1
        			// 범위 안에 참가자 한 명 이상과 출구가 존재하는지!!
        			
        			for (Member m : members) { // 범위 안에 참가자가 있음
        				if (m.isFinished) continue;
        				if (r <= m.r && m.r <= r+size-1 && c <= m.c && m.c <= c+size-1) {
        					memberExist = true;
        				}
        			}
        			
        			if (r <= endR && endR <= r+size-1 && c <= endC && endC <= c+size-1) {
        				exitExist = true;
        			}
        			
        			if (memberExist && exitExist) {
        				return new int[] {r,c,size};
        			}
        		}
        	}
    	}
    	
    	return new int[] {0,0,0};
    }
    
    private static void moveMember() {
    	// 동시 이동에 영향 주지 않기 위해 임시 맵 생성
    	int[][] tmpMap = new int[N+1][N+1];

    	for (int i = 0; i < members.size(); i++) {
    		Member now = members.get(i);
    		if (now.isFinished) continue; // 탈출 성공한 사람은 넘어가기
    		
    		int nowDistance = Math.abs(now.r - endR) + Math.abs(now.c - endC);
    		
    		//System.out.println("현재 위치 : " + now.r + " " + now.c);
    		int[] dirDistance = new int[4]; // 거리 배열
    		
    		// 상하좌우 이동, 상하 우선
    		for (int k = 0; k < 4; k++) {
    			int nextY = now.r + dy[k];
    			int nextX = now.c + dx[k];
    			int nextDistance = Math.abs(nextY - endR) + Math.abs(nextX - endC);
    			
    			// 범위 체크
    			if (nextY <= 0 || nextY > N || nextX <= 0 || nextX > N) {
    				dirDistance[k] = -1; // 갈 수 없음
    			} else {
    				dirDistance[k] = nextDistance;
    			}
    		}
    		
    		boolean isMoved = false;
    		int newDy = 0;
    		int newDx = 0;
    		for (int d = 0; d < dirDistance.length; d++) {
    			if (dirDistance[d] == -1) continue; // 경로 이탈
    			if (1 <= map[now.r+dy[d]][now.c+dx[d]] && map[now.r+dy[d]][now.c+dx[d]] <= 9) continue; // 벽은 갈 수 없음
    			if (dirDistance[d] < nowDistance) {
    				nowDistance = dirDistance[d];
    				isMoved = true;
    				newDy = dy[d];
    				newDx = dx[d];
    			}
    		}
    		
    		now.r += newDy;
    		now.c += newDx;
    		
    		//System.out.println("갱신 위치 : " + now.r + " " + now.c);
    		
    		// 이동했으면 이동 거리에 추가
    		if (isMoved) {
    			now.distance++;
    		}
    		
    		// 출구에 도착했으면 임시 맵에 기록 안함
    		if (now.r == endR && now.c == endC) {
    			now.isFinished = true;
    			continue;
    		}
    		
    		// 임시 맵에 기록
    		tmpMap[now.r][now.c] += -(i+1);
    	}
    	
    	// tmpMap 에 나머지 맵 정보 기록하기
    	for (int i = 1; i <= N; i++) {
    		for (int j = 1; j <= N; j++) {
    			// 벽을 기록한다
    			if (1 <= map[i][j] && map[i][j] <= 9) {
    				tmpMap[i][j] = map[i][j];
    			}
    		}
    	}
    	tmpMap[endR][endC] = -100; // 출구 기록
    	
    	// 원래 맵에 옮겨적기
    	for (int i = 1; i <= N; i++) {
    		for (int j = 1; j <= N; j++) {
    			map[i][j] = tmpMap[i][j];
    		}
    	}
    	
    	//printMap(map);
    }
    
    private static void printMap(int[][] map) {
    	for (int i = 1; i < map.length; i++) {
    		for (int j = 1; j < map[0].length; j++) {
    			System.out.print(map[i][j] + " ");
    		}
    		System.out.println();
    	}
    	System.out.println();
    }
}