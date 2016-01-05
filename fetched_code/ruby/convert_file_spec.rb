# encoding: utf-8

RSpec.describe TTY::Prompt::Question, 'convert file' do
  it "converts to file" do
    file = double(:file)
    allow(File).to receive(:open).with(/test\.txt/).and_return(file)
    prompt = TTY::TestPrompt.new
    prompt.input << "test.txt"
    prompt.input.rewind
    answer = prompt.ask("Which file to open?", convert: :file)
    expect(answer).to eq(file)
    expect(File).to have_received(:open).with(/test\.txt/)
  end
end
