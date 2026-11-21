package backend;
public class GestorAlgoritmos {

    // Algoritmo de Búsqueda Binaria (Tema 01)
    public static int busquedaBinaria(int[] arregloOrdenado, int valor) {
        int baja = 0;
        int alta = arregloOrdenado.length - 1;

        while (baja <= alta) {
            int media = baja + (alta - baja) / 2;
            if (arregloOrdenado[media] == valor) return media;
            if (arregloOrdenado[media] < valor) baja = media + 1;
            else alta = media - 1;
        }
        return -1;
    }

    // Algoritmo de Ordenamiento por Seleccion (Selection Sort) (Tema 01)
    public static void ordenamientoSeleccion(int[] arreglo) {
        int n = arreglo.length;
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (arreglo[j] < arreglo[minIdx]) {
                    minIdx = j;
                }
            }
            int temp = arreglo[minIdx];
            arreglo[minIdx] = arreglo[i];
            arreglo[i] = temp;
        }
    }
}