require 'spec_helper'

describe Banzai::CrossProjectReference, lib: true do
  include described_class

  describe '#project_from_ref' do
    context 'when no project was referenced' do
      it 'returns the project from context' do
        project = double

        allow(self).to receive(:context).and_return({ project: project })

        expect(project_from_ref(nil)).to eq project
      end
    end

    context 'when referenced project does not exist' do
      it 'returns nil' do
        expect(project_from_ref('invalid/reference')).to be_nil
      end
    end

    context 'when referenced project exists' do
      it 'returns the referenced project' do
        project2 = double('referenced project')

        expect(Project).to receive(:find_with_namespace).
          with('cross/reference').and_return(project2)

        expect(project_from_ref('cross/reference')).to eq project2
      end
    end
  end
end
