package com.nextlabs.bulkprotect.filesupplier;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.commons.vfs2.FileObject;

import com.nextlabs.bulkprotect.util.FileSkippedException;
import com.nextlabs.bulkprotect.util.Result;

public class FileSupplierFilter implements Supplier<Optional<Result<FileObject, Exception>>> {

	protected Collection<Predicate<FileObject>> preds;
	protected Supplier<Optional<Result<FileObject, Exception>>> fileSupplier;

	private Map<String, Integer> extCount;

	public FileSupplierFilter(Supplier<Optional<Result<FileObject, Exception>>> fileSupplier, Predicate<FileObject> pred) {
		this.preds = new ArrayList<>();
		this.preds.add(pred);
		this.fileSupplier = fileSupplier;

		extCount = new HashMap<>();
	}

	public FileSupplierFilter(Supplier<Optional<Result<FileObject, Exception>>> fileSupplier, Collection<Predicate<FileObject>> preds) {
		this.preds = preds;
		this.fileSupplier = fileSupplier;
	}

	@Override
	public Optional<Result<FileObject, Exception>> get() {
		Optional<Result<FileObject, Exception>> candidate;
		while (true) {
			candidate = fileSupplier.get();
			if (candidate.isPresent() && candidate.get().isOk()) countExtension(candidate.get().getValue().toString());
			if (candidate.isPresent() && candidate.get().isOk() && !preds.stream().allMatch(p -> p.test(candidate.get().getValue())))
				return Optional.of(Result.err(new FileSkippedException("File criteria not met")));
			else return candidate;
		}
	}

	@Override
	public String toString() { return fileSupplier.toString(); }

	private void countExtension(String path) {
		String[] pathSplit = path.split("/");
		String fileName = pathSplit[pathSplit.length-1];

		String[] nameParts = fileName.split("\\.");
		String ext = nameParts.length > 1 ? nameParts[nameParts.length-1].toLowerCase() : "";

		extCount.putIfAbsent(ext, 0);
		extCount.put(ext, extCount.get(ext)+1);
	}

	public Map<String, Integer> getExtCount() { return extCount; }
}
