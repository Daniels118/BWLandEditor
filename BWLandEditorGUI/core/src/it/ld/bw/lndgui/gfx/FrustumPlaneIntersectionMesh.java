package it.ld.bw.lndgui.gfx;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import java.util.Comparator;

public class FrustumPlaneIntersectionMesh implements Disposable {
    //vertex layout: position(3), normal(3), uv(2) = 8 float
    private static final int STRIDE = 8;
    private final float uvScale;

    private Mesh mesh;

    //buffers
    private final Array<Vector3> intersections = new Array<>(true, 16);
    private final Array<Vector3> hull = new Array<>(true, 16);

    public FrustumPlaneIntersectionMesh(float uvScale) {
        this.uvScale = uvScale;

        //isStatic = false since we're going to update it every frame
        mesh = new Mesh(false, 16, 48,
                VertexAttribute.Position(),
                VertexAttribute.Normal(),
                VertexAttribute.TexCoords(0));
    }

    public Mesh getMesh() {
    	if (mesh == null) {
    		throw new IllegalStateException(this.getClass().getSimpleName()+" has been disposed");
    	}
        return mesh;
    }

    /**
     * Update the mesh with the intersection between frustum and plane y=0.
     * Return true if the mesh is valid (at least 3 vertices).
     */
    public boolean update(Camera cam) {
        intersections.clear();

        //frustum corners in this order: nearTL, nearTR, nearBR, nearBL, farTL, farTR, farBR, farBL
        Vector3[] p = cam.frustum.planePoints;

        // 12 edges of frustum-box
        // near edges
        addEdgeIntersectionY0(p[0], p[1]);
        addEdgeIntersectionY0(p[1], p[2]);
        addEdgeIntersectionY0(p[2], p[3]);
        addEdgeIntersectionY0(p[3], p[0]);

        // far edges
        addEdgeIntersectionY0(p[4], p[5]);
        addEdgeIntersectionY0(p[5], p[6]);
        addEdgeIntersectionY0(p[6], p[7]);
        addEdgeIntersectionY0(p[7], p[4]);

        // connecting edges
        addEdgeIntersectionY0(p[0], p[4]);
        addEdgeIntersectionY0(p[1], p[5]);
        addEdgeIntersectionY0(p[2], p[6]);
        addEdgeIntersectionY0(p[3], p[7]);

        // remove duplicates (could happen if a vertex lays on y=0 or with numeric errors)
        dedupe(intersections, 1e-5f);

        if (intersections.size < 3) {
            return false;
        }

        // convex hull in XZ (CCW polygon seen from above, normal: +Y)
        hull.clear();
        convexHullXZ(intersections, hull);

        if (hull.size < 3) {
            return false;
        }

        // build buffers vertices/indices
        int n = hull.size;
        if (n > 16) {
        	return false;
        }
        float[] verts = new float[n * STRIDE];
        short[] idx = new short[(n - 2) * 3];

        // Vertices: y=0, normal (0,1,0), uv from (x,z)
        for (int i = 0; i < n; i++) {
            Vector3 v = hull.get(i);

            int base = i * STRIDE;
            verts[base + 0] = v.x;
            verts[base + 1] = 0f;
            verts[base + 2] = v.z;

            verts[base + 3] = 0f;
            verts[base + 4] = 1f;
            verts[base + 5] = 0f;

            verts[base + 6] = v.x * uvScale;
            verts[base + 7] = v.z * uvScale;
        }

        // "fan" triangulation: (0, i, i+1)
        int k = 0;
        for (int i = 1; i < n - 1; i++) {
            idx[k++] = 0;
            idx[k++] = (short) i;
            idx[k++] = (short) (i + 1);
        }

        mesh.setVertices(verts, 0, verts.length);
        mesh.setIndices(idx, 0, idx.length);

        return true;
    }

    // -----------------------------
    // Intersect segment with plane y=0
    // -----------------------------

    private void addEdgeIntersectionY0(Vector3 a, Vector3 b) {
        // check if the segment pass through plane y=0
        float ya = a.y;
        float yb = b.y;

        // both on same side, no intersection
        if ((ya > 0f && yb > 0f) || (ya < 0f && yb < 0f)) return;

        // both on the plane: ignore
        if (Math.abs(ya) < 1e-8f && Math.abs(yb) < 1e-8f) return;

        // compute t such as y(t)=0: a + t*(b-a)
        float t = ya / (ya - yb);
        if (t < 0f || t > 1f) return;

        float x = a.x + (b.x - a.x) * t;
        float z = a.z + (b.z - a.z) * t;

        intersections.add(new Vector3(x, 0f, z));
    }

    // -----------------------------
    // Dedupe (remove close points)
    // -----------------------------

    private static void dedupe(Array<Vector3> pts, float eps) {
        for (int i = 0; i < pts.size; i++) {
            Vector3 pi = pts.get(i);
            for (int j = pts.size - 1; j > i; j--) {
                Vector3 pj = pts.get(j);
                if (Math.abs(pi.x - pj.x) <= eps && Math.abs(pi.z - pj.z) <= eps) {
                    pts.removeIndex(j);
                }
            }
        }
    }

    // -----------------------------
    // Convex hull in XZ (Monotonic Chain)
    // output CCW (seen from above, +Y)
    // -----------------------------

    private static void convexHullXZ(Array<Vector3> input, Array<Vector3> output) {
        Array<Vector3> pts = new Array<>(input);
        pts.sort(new Comparator<Vector3>() {
            @Override public int compare(Vector3 a, Vector3 b) {
                if (a.x < b.x) return -1;
                if (a.x > b.x) return 1;
                return Float.compare(a.z, b.z);
            }
        });

        Array<Vector3> lower = new Array<>(true, pts.size);
        for (int i = 0; i < pts.size; i++) {
            Vector3 p = pts.get(i);
            while (lower.size >= 2 && crossXZ(lower.get(lower.size - 2), lower.get(lower.size - 1), p) <= 0f) {
                lower.pop();
            }
            lower.add(p);
        }

        Array<Vector3> upper = new Array<>(true, pts.size);
        for (int i = pts.size - 1; i >= 0; i--) {
            Vector3 p = pts.get(i);
            while (upper.size >= 2 && crossXZ(upper.get(upper.size - 2), upper.get(upper.size - 1), p) <= 0f) {
                upper.pop();
            }
            upper.add(p);
        }

        // concatenate without repeat endpoints
        output.clear();
        for (int i = 0; i < lower.size - 1; i++) output.add(lower.get(i));
        for (int i = 0; i < upper.size - 1; i++) output.add(upper.get(i));
    }

    // 2D cross product 2D on XZ: (b-a) x (c-a)
    // positive => turn left (CCW)
    private static float crossXZ(Vector3 a, Vector3 b, Vector3 c) {
        float abx = b.x - a.x;
        float abz = b.z - a.z;
        float acx = c.x - a.x;
        float acz = c.z - a.z;
        return abx * acz - abz * acx;
    }

	@Override
	public void dispose() {
		mesh.dispose();
		mesh = null;
	}
}
