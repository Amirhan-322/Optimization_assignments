import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("S: ");
        int[] supply = readVector(scanner);

        System.out.println("C:");
        int[][] costMatrix = readMatrix(scanner, supply.length);

        System.out.print("D: ");
        int[] demand = readVector(scanner);

        checkBalance(supply,demand);
        printInputTable(costMatrix, supply, demand);
        System.out.println("Initial basic feasible solutions:");
        northWest(costMatrix,supply,demand);
        vogel(costMatrix,supply,demand);
        russell(costMatrix,supply,demand);

    }

    private static void checkBalance(int[] supply, int[] demand){
        int value = 0;
        for(int i = 0; i < 3; i++){
            value+=supply[i];
        }
        for(int i = 0; i < 4; i++){
            value-=demand[i];
        }
        if(value!=0){
            System.out.println("The problem is not balanced!");
            System.exit(0);
        }
    }


    private static void printInputTable(int[][] costMatrix, int[] supply, int[] demand) {
        int rows = costMatrix.length;
        int cols = costMatrix[0].length;
        System.out.println();
        System.out.print("   ");
        for (int j = 0; j < cols; j++) {
            System.out.printf("D%d  ", j + 1);
        }
        System.out.println("Supply");

        for (int i = 0; i < rows; i++) {
            System.out.printf("S%d  ", i + 1);
            for (int j = 0; j < cols; j++) {
                System.out.printf("%d  ", costMatrix[i][j]);
            }
            System.out.printf("%d\n", supply[i]);
        }

        System.out.print("Demand: ");
        for (int j = 0; j < cols; j++) {
            System.out.printf("%d  ", demand[j]);
        }
        System.out.println();
        System.out.println();
    }

    private static int[] readVector(Scanner scanner) {
        String input = scanner.nextLine();
        String[] parts = input.split(" ");
        int[] vector = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            vector[i] = Integer.parseInt(parts[i]);
        }
        return vector;
    }


    public static int[][] readMatrix(Scanner scanner, int rows) {
        int[][] matrix = new int[rows][];
        for (int i = 0; i < rows; i++) {
            String[] input = scanner.nextLine().split(" ");
            matrix[i] = new int[input.length];
            for (int j = 0; j < input.length; j++) {
                matrix[i][j] = Integer.parseInt(input[j]);
            }
        }
        return matrix;
    }

    public static void printMatrix(int[][] matrix) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                System.out.printf("%d ", matrix[i][j]);
            }
            System.out.println();
        }
        System.out.println();
    }

    public static int[][] northWest(int[][] cost, int[] supply, int[] demand){
        int[][] result = new int[3][4];
        int[] newDemand = new int[4];
        int[] newSupply = new int[3];
        for(int i = 0; i<3; i++){
            for(int j = 0; j<4; j++){
                result[i][j] = 0;
            }
            newSupply[i] = supply[i];
        }
        for(int j = 0; j<4; j++){
            newDemand[j] = demand[j];
        }
        int curSup = 0,curDem = 0;

        while(curSup <= 2 && curDem <=3){

            if(newDemand[curDem]<newSupply[curSup]){
                result[curSup][curDem] = newDemand[curDem];
                newSupply[curSup] = newSupply[curSup]-newDemand[curDem];
                newDemand[curDem] = 0;
                curDem++;
            } else if (newDemand[curDem]>newSupply[curSup]) {
                result[curSup][curDem] = newSupply[curSup];
                newDemand[curDem] = newDemand[curDem]-newSupply[curSup];
                newSupply[curSup] = 0;
                curSup++;
            } else {
                result[curSup][curDem] = newSupply[curSup];
                curSup++;
                curDem++;
            }
        }
        System.out.println("1.Using North-West corner method:");
        printMatrix(result);
        return result;
    }

    public static void findDiff(int[] ColDif, int[] RowDif, int[][] cost, int[] newDemand, int[] newSupply) {
        for(int i = 0; i < 4; i++){
            if(newDemand[i]==0){
                ColDif[i]=-1;
                continue;
            }
            int min1 = Integer.MAX_VALUE;
            int min2 = Integer.MAX_VALUE;
            for(int j = 0; j < 3; j++){
                if(min2>cost[j][i] && newSupply[j]!=0){
                    min2 = cost[j][i];
                    if(min1>min2){
                        min2 = min1;
                        min1 = cost[j][i];
                    }
                }
            }
            if(min2 != Integer.MAX_VALUE) {
                ColDif[i] = min2 - min1;
            }else{
                ColDif[i] = Integer.MAX_VALUE;
            }
        }
        for(int i = 0; i < 3; i++){
            if(newSupply[i]==0){
                RowDif[i]=-1;
                continue;
            }
            int min1 = Integer.MAX_VALUE;
            int min2 = Integer.MAX_VALUE;
            for(int j = 0; j < 4; j++){
                if(min2>cost[i][j] && newDemand[j]!=0){
                    min2 = cost[i][j];
                    if(min1>min2){
                        min2 = min1;
                        min1 = cost[i][j];
                    }
                }
            }
            if(min2 != Integer.MAX_VALUE) {
                RowDif[i] = min2-min1;
            }else{
                RowDif[i] = Integer.MAX_VALUE;
            }

        }
    }

    public static int[][] vogel(int[][] cost, int[] supply, int[] demand){
        int[][] result = new int[3][4];
        int[] newDemand = new int[4];
        int[] newSupply = new int[3];
        for(int i = 0; i<3; i++){
            for(int j = 0; j<4; j++){
                result[i][j] = 0;
            }
            newSupply[i] = supply[i];
        }
        for(int j = 0; j<4; j++){
            newDemand[j] = demand[j];
        }
        int[] ColDif = new int[4];
        int[] RowDif = new int[3];
        while(true){
            findDiff(ColDif,RowDif,cost,newDemand,newSupply);
            int maxDif=-1;
            int index = -1;
            boolean isCol = true;
            for(int i = 0; i < 3; i++){
                if(RowDif[i]>maxDif){
                    maxDif = RowDif[i];
                    isCol = false;
                    index = i;
                }
            }
            for(int i = 0; i < 4; i++){
                if(ColDif[i]>maxDif){
                    maxDif = ColDif[i];
                    isCol = true;
                    index = i;
                }
            }
            int curSup = -1,curDem = -1;
            if(isCol){
                curDem = index;
                int min = Integer.MAX_VALUE;
                for(int i = 0; i < 3; i++) {
                    if (cost[i][curDem] < min && RowDif[i] != -1) {
                        min = cost[i][curDem];
                        curSup = i;
                    }
                }
                if(newDemand[curDem]<newSupply[curSup]){
                    result[curSup][curDem] = newDemand[curDem];
                    newSupply[curSup] = newSupply[curSup]-newDemand[curDem];
                    newDemand[curDem] = 0;
                } else if (newDemand[curDem]>newSupply[curSup]) {
                    result[curSup][curDem] = newSupply[curSup];
                    newDemand[curDem] = newDemand[curDem]-newSupply[curSup];
                    newSupply[curSup] = 0;
                } else {
                    result[curSup][curDem] = newSupply[curSup];
                    newDemand[curDem] = 0;
                    newSupply[curSup] = 0;
                }
            }else{
                curSup = index;
                int min = Integer.MAX_VALUE;
                for(int i = 0; i < 4; i++) {
                    if (cost[curSup][i] < min && ColDif[i] != -1) {
                        min = cost[curSup][i];
                        curDem = i;
                    }
                }
                if(newDemand[curDem]<newSupply[curSup]){
                    result[curSup][curDem] = newDemand[curDem];
                    newSupply[curSup] = newSupply[curSup]-newDemand[curDem];
                    newDemand[curDem] = 0;
                } else if (newDemand[curDem]>newSupply[curSup]) {
                    result[curSup][curDem] = newSupply[curSup];
                    newDemand[curDem] = newDemand[curDem]-newSupply[curSup];
                    newSupply[curSup] = 0;
                } else {
                    result[curSup][curDem] = newSupply[curSup];
                    newDemand[curDem] = 0;
                    newSupply[curSup] = 0;
                }
            }

            boolean Flag = true;
            for(int i = 0;i < 3; i++){
                if(newSupply[i] !=0){
                    Flag = false;
                    break;
                }
            }
            for(int i = 0;i < 4; i++){
                if(!Flag){
                    break;
                }
                if(newDemand[i] !=0){
                    Flag = false;
                }
            }
            if(Flag){
                break;
            }
        }

        System.out.println("2.Using Vogel’s approximation method:");
        printMatrix(result);
        return result;
    }


    public static int[][] russell(int[][] cost, int[] supply, int[] demand) {
        int[][] result = new int[3][4];
        int[] newDemand = new int[4];
        int[] newSupply = new int[3];
        for(int i = 0; i<3; i++){
            for(int j = 0; j<4; j++){
                result[i][j] = 0;
            }
            newSupply[i] = supply[i];
        }
        for(int j = 0; j<4; j++){
            newDemand[j] = demand[j];
        }
        int[] U = new int[3];
        int[] V = new int[4];
        while(true){
            for(int i = 0; i < 3; i++){
                if(newSupply[i]==0){
                    continue;
                }
                U[i]=Integer.MIN_VALUE;
                for(int j = 0; j < 4; j++){
                    if(newDemand[j]==0){
                        continue;
                    }
                    if(U[i]<cost[i][j] && newSupply[i]!=0 && newDemand[j]!=0){
                        U[i]=cost[i][j];
                    }
                }
            }
            for(int i = 0; i < 4; i++){
                if(newDemand[i]==0){
                    continue;
                }
                V[i]=Integer.MIN_VALUE;
                for(int j = 0; j < 3; j++){
                    if(newSupply[j]==0){
                        continue;
                    }
                    if(V[i]<cost[j][i]){
                        V[i]=cost[j][i];
                    }
                }
            }

            int max = 0;
            int curSup = -1, curDem = -1;
            for(int i = 0; i < 3; i++){
                if(newSupply[i]==0){
                    continue;
                }
                for(int j = 0; j < 4; j++){
                    if(newDemand[j]==0){
                        continue;
                    }
                    int value = Math.abs(cost[i][j] - U[i] - V[j]);
                    if(value>max){
                        max = value;
                        curSup = i;
                        curDem = j;
                    }
                }
            }

            if(newDemand[curDem]<newSupply[curSup]){
                result[curSup][curDem] = newDemand[curDem];
                newSupply[curSup] = newSupply[curSup]-newDemand[curDem];
                newDemand[curDem] = 0;
            } else if (newDemand[curDem]>newSupply[curSup]) {
                result[curSup][curDem] = newSupply[curSup];
                newDemand[curDem] = newDemand[curDem]-newSupply[curSup];
                newSupply[curSup] = 0;
            } else {
                result[curSup][curDem] = newSupply[curSup];
                newDemand[curDem] = 0;
                newSupply[curSup] = 0;
            }

            boolean Flag = true;
            for(int i = 0;i < 3; i++){
                if(newSupply[i] !=0){
                    Flag = false;
                    break;
                }
            }
            for(int i = 0;i < 4; i++){
                if(!Flag){
                    break;
                }
                if(newDemand[i] !=0){
                    Flag = false;
                }
            }
            if(Flag){
                break;
            }

        }

        System.out.println("3.Using Russell’s approximation method:");
        printMatrix(result);
        return result;
    }
}