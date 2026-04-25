import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.Position;

public class MethodInfo {
    public final String name;
    public final transient MethodDeclaration method; // используем для получения границ
    public boolean hasMarker;
    public final int order;
    public final int weight;

    public MethodInfo(String name, MethodDeclaration method, boolean hasMarker, int order) {
        this.name = name;
        this.method = method;
        this.hasMarker = hasMarker;
        this.order = order;
        this.weight = method.getBody()
                .map(body -> body.findAll(com.github.javaparser.ast.Node.class).size())
                .orElse(0);
    }
}