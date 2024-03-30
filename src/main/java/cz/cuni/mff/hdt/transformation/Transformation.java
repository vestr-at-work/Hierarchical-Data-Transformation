package cz.cuni.mff.hdt.transformation;

import cz.cuni.mff.hdt.adapter.SinkSourceAdapter;
import cz.cuni.mff.hdt.sink.Sink;
import cz.cuni.mff.hdt.source.DocumentSource;
import cz.cuni.mff.hdt.transformation.operations.OperationFailedException;

/**
 * Core class for transformation of the hierarchical data 
 */
public class Transformation {
    private TransformationContext context;
    SinkSourceAdapter firstAdapter = new SinkSourceAdapter();
    SinkSourceAdapter secondAdapter = new SinkSourceAdapter();

    public Transformation(TransformationContext context) {
        this.context = context; 
    }

    /*
     * Main public function for transforming the data
     */
    public void transform() throws OperationFailedException {
        TransformationDefinition definition = context.transformationFile(); 

        for (int i = 0; i < definition.operations.size(); i++) {
            DocumentSource operationSource = getSource(i, definition.operations.size());
            Sink opertionSink = getSink(i, definition.operations.size());
            var operation = definition.operations.get(i);

            operation.execute(operationSource, opertionSink);
        }

        // TODO: close source
        context.outputSink().flush();
    }

    private DocumentSource getSource(Integer index, Integer operationsSize) {
        if (index == 0) {
            return context.inputSource();
        }
        if (index % 2 == 0) {
            return secondAdapter;
        }
        return firstAdapter;
    }

    private Sink getSink(Integer index, Integer operationsSize) {
        if (index == operationsSize - 1) {
            return context.outputSink();
        }
        if (index % 2 == 0) {
            return firstAdapter;
        }
        return secondAdapter;
    }
}